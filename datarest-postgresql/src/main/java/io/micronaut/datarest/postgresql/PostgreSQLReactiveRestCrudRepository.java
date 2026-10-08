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
package io.micronaut.datarest.postgresql;

import io.micronaut.context.annotation.EachBean;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.ReactiveRestCrudRepository;
import io.micronaut.http.client.HttpClient;
import org.jspecify.annotations.Nullable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

/**
 * {@link ReactiveRestCrudRepository} implementation talking to a <a href="https://postgrest.org">PostgREST</a> API.
 * A bean is created for every {@link RestDataSourceClient}. Every publisher emits at most one item.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@EachBean(RestDataSourceClient.class)
public final class PostgreSQLReactiveRestCrudRepository implements ReactiveRestCrudRepository {
    private final HttpClient client;

    /**
     * @param restDataSourceClient client bound to the PostgREST data source
     */
    public PostgreSQLReactiveRestCrudRepository(RestDataSourceClient restDataSourceClient) {
        this.client = restDataSourceClient.getHttpClient();
    }

    @Override
    @SingleResult
    public <T> Publisher<T> save(String table, Object row, Class<T> type) {
        return Mono.from(client.retrieve(PostgrestRequests.save(table, row), Argument.listOf(type)))
            .map(PostgrestResponses::single);
    }

    @Override
    @SingleResult
    public <T> Publisher<Page<T>> findAll(String table, Class<T> type) {
        return findAll(table, (PostgrestQuery) null, type);
    }

    @Override
    @SingleResult
    public <T> Publisher<Page<T>> findAll(String table, Pageable pageable, Class<T> type) {
        return Mono.from(client.exchange(PostgrestRequests.findAll(table, pageable), Argument.listOf(type)))
            .map(response -> PostgrestResponses.page(response, pageable));
    }

    /**
     * Lists the rows of a table matching a query.
     *
     * @param table table name
     * @param query filters, ordering and pagination; {@code null} for every row
     * @param type  type to deserialize each row into
     * @param <T>   row type
     * @return a page of rows
     */
    @SingleResult
    public <T> Publisher<Page<T>> findAll(String table, @Nullable PostgrestQuery query, Class<T> type) {
        return Mono.from(client.exchange(PostgrestRequests.findAll(table, query), Argument.listOf(type)))
            .map(response -> PostgrestResponses.page(response, query));
    }

    @Override
    @SingleResult
    public <T> Publisher<T> findById(String table, String idColumn, Object id, Class<T> type) {
        return Mono.from(client.retrieve(PostgrestRequests.findById(table, idColumn, id), Argument.listOf(type)))
            .mapNotNull(PostgrestResponses::firstOrNull);
    }

    @Override
    @SingleResult
    public <T> Publisher<T> update(String table, String idColumn, Object id, Object row, Class<T> type) {
        return Mono.from(client.retrieve(PostgrestRequests.update(table, idColumn, id, row), Argument.listOf(type)))
            .mapNotNull(PostgrestResponses::firstOrNull);
    }

    @Override
    @SingleResult
    public Publisher<Long> count(String table) {
        return count(table, null);
    }

    /**
     * Counts the rows matching a query without fetching them.
     *
     * @param table table name
     * @param query filters; {@code null} for every row
     * @return the number of matching rows
     */
    @SingleResult
    public Publisher<Long> count(String table, @Nullable PostgrestQuery query) {
        return Mono.from(client.exchange(PostgrestRequests.count(table, query)))
            .map(PostgrestResponses::count);
    }

    @Override
    @SingleResult
    public Publisher<Boolean> existsById(String table, String idColumn, Object id) {
        return Mono.from(client.exchange(PostgrestRequests.existsById(table, idColumn, id)))
            .map(response -> PostgrestResponses.count(response) > 0);
    }

    @Override
    @SingleResult
    public Publisher<Integer> deleteById(String table, String idColumn, Object id) {
        return Mono.from(client.retrieve(PostgrestRequests.deleteById(table, idColumn, id), PostgrestResponses.ROWS))
            .map(rows -> rows.size());
    }

    /**
     * Deletes the rows matching a query.
     *
     * @param table table name
     * @param query filters selecting the rows to delete; must not be empty
     * @return the number of deleted rows
     * @throws IllegalArgumentException if the query has no filters
     */
    @SingleResult
    public Publisher<Integer> deleteAll(String table, PostgrestQuery query) {
        return Mono.from(client.retrieve(PostgrestRequests.deleteAll(table, query), PostgrestResponses.ROWS))
            .map(rows -> rows.size());
    }
}
