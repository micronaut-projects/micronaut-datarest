/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.datarest.ords;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.ReactiveRestGenericRepository;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.tree.JsonNode;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * {@link ReactiveRestGenericRepository} implementation talking to an
 * <a href="https://docs.oracle.com/en/database/oracle/oracle-rest-data-services/">ORDS AutoREST</a> API.
 * A bean is created for every {@link RestDataSourceClient}. Every publisher emits at most one item.
 *
 * <p>Rows are addressed by primary key, so the {@code idColumn} arguments are accepted for API
 * compatibility but not used: ORDS exposes rows at {@code /table/id}.</p>
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public final class OrdsReactiveRestGenericRepository implements ReactiveRestGenericRepository {
    /**
     * Window used to count rows, since AutoREST collections carry no total.
     */
    private static final int COUNT_WINDOW = 500;
    private static final Argument<JsonNode> JSON = Argument.of(JsonNode.class);
    private final HttpClient client;
    private final OrdsRequests requests;
    private final OrdsResponses responses;
    /**
     * {@code DATE} columns per table, read once from the metadata catalog.
     */
    private final Map<String, Mono<Set<String>>> dateColumns = new ConcurrentHashMap<>();

    /**
     * @param restDataSourceClient client bound to the ORDS data source
     * @param jsonMapper           mapper used to bind rows
     */
    public OrdsReactiveRestGenericRepository(RestDataSourceClient restDataSourceClient, JsonMapper jsonMapper) {
        this.client = restDataSourceClient.getHttpClient();
        this.requests = new OrdsRequests(jsonMapper);
        this.responses = new OrdsResponses(jsonMapper);
    }

    @Override
    @SingleResult
    public <T> Publisher<T> save(String table, Object row, Class<T> type) {
        return dateColumns(table).flatMap(dates ->
            Mono.from(client.retrieve(requests.save(table, OrdsResponses.toOrds(responses.tree(row), dates)), JSON))
                .map(document -> responses.row(document, dates, type)));
    }

    @Override
    @SingleResult
    public <T> Publisher<Page<T>> findAll(String table, Class<T> type) {
        return fetchPage(table, null, type);
    }

    @Override
    @SingleResult
    public <T> Publisher<Page<T>> findAll(String table, Pageable pageable, Class<T> type) {
        return fetchPage(table, pageable, type);
    }

    private <T> Mono<Page<T>> fetchPage(String table, @Nullable Pageable pageable, Class<T> type) {
        return dateColumns(table).flatMap(dates ->
            Mono.from(client.retrieve(requests.findAll(table, pageable), JSON))
                .map(document -> responses.collection(document, dates, type))
                .flatMap(collection -> total(table, collection)
                    .map(total -> Page.of(collection.items(), pageable(pageable, collection), total))));
    }

    @Override
    @SingleResult
    public <T> Publisher<T> findById(String table, String idColumn, Object id, Class<T> type) {
        return dateColumns(table).flatMap(dates ->
            rowOrEmpty(table, id).map(document -> responses.row(document, dates, type)));
    }

    @Override
    @SingleResult
    public <T> Publisher<T> update(String table, String idColumn, Object id, Object row, Class<T> type) {
        // AutoREST PUT replaces the whole row, so merge the given columns into the current ones.
        return dateColumns(table).flatMap(dates -> rowOrEmpty(table, id)
            .map(responses::columns)
            .flatMap(columns -> {
                columns.putAll(responses.columns(row));
                JsonNode body = OrdsResponses.toOrds(responses.tree(columns), dates);
                return Mono.from(client.retrieve(requests.update(table, id, body), JSON));
            })
            .map(document -> responses.row(document, dates, type)));
    }

    /**
     * Reads the table's {@code DATE} columns from the metadata catalog, once, so that ISO dates can be
     * translated to and from the timestamps ORDS uses for them.
     */
    private Mono<Set<String>> dateColumns(String table) {
        return dateColumns.computeIfAbsent(table, t ->
            Mono.from(client.retrieve(requests.metadata(t), JSON))
                .map(OrdsResponses::dateColumns)
                .cache());
    }

    @Override
    @SingleResult
    public Publisher<Long> count(String table) {
        return countFrom(table, 0);
    }

    @Override
    @SingleResult
    public Publisher<Boolean> existsById(String table, String idColumn, Object id) {
        return rowOrEmpty(table, id).map(document -> true).defaultIfEmpty(false);
    }

    @Override
    @SingleResult
    public Publisher<Integer> deleteById(String table, String idColumn, Object id) {
        return Mono.from(client.retrieve(requests.deleteById(table, id), JSON))
            .map(responses::rowsDeleted);
    }

    /**
     * Fetches a row, completing empty on {@code 404}.
     */
    private Mono<JsonNode> rowOrEmpty(String table, Object id) {
        return Mono.from(client.retrieve(requests.findById(table, id), JSON))
            .onErrorResume(OrdsReactiveRestGenericRepository::isNotFound, e -> Mono.empty());
    }

    /**
     * Walks the collection in windows, summing the rows, because AutoREST reports no total.
     */
    private Mono<Long> countFrom(String table, long offset) {
        return Mono.from(client.retrieve(requests.findAll(table, COUNT_WINDOW, offset), JSON))
            .map(document -> responses.collection(document, Set.of(), Map.class))
            .flatMap(window -> {
                long seen = offset + window.items().size();
                return window.hasMore() && !window.items().isEmpty() ? countFrom(table, seen) : Mono.just(seen);
            });
    }

    /**
     * The total is known from the window alone when there are no more rows; otherwise it is counted.
     */
    private <T> Mono<Long> total(String table, OrdsResponses.Collection<T> collection) {
        long end = collection.offset() + collection.items().size();
        return collection.hasMore() ? countFrom(table, end) : Mono.just(end);
    }

    private static Pageable pageable(@Nullable Pageable requested, OrdsResponses.Collection<?> collection) {
        if (requested != null) {
            return requested;
        }
        return collection.limit() > 0
            ? Pageable.from((int) (collection.offset() / collection.limit()), collection.limit())
            : Pageable.unpaged();
    }

    private static boolean isNotFound(Throwable throwable) {
        return throwable instanceof HttpClientResponseException e && e.getStatus() == HttpStatus.NOT_FOUND;
    }
}
