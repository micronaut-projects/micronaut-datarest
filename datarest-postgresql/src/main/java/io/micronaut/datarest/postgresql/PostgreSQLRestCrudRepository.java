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
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.RestCrudRepository;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.uri.UriBuilder;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * {@link RestCrudRepository} implementation talking to a <a href="https://postgrest.org">PostgREST</a> API.
 * A bean is created for every {@link RestDataSourceClient}.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@EachBean(RestDataSourceClient.class)
public final class PostgreSQLRestCrudRepository implements AutoCloseable, RestCrudRepository {
    /**
     * Query parameter selecting the columns to return.
     */
    public static final String SELECT = "select";
    /**
     * Query parameter ordering the rows.
     */
    public static final String ORDER = "order";
    /**
     * Query parameter limiting the number of rows.
     */
    public static final String LIMIT = "limit";
    /**
     * Query parameter skipping rows.
     */
    public static final String OFFSET = "offset";
    private static final String SLASH = "/";
    private static final String PREFER = "Prefer";
    private static final String RETURN_REPRESENTATION = "return=representation";
    private static final String COUNT_EXACT = "count=exact";
    private static final String EQ = "eq.";
    private static final Argument<List<Map<String, Object>>> ROWS =
            Argument.listOf(Argument.mapOf(String.class, Object.class));
    private final BlockingHttpClient client;

    /**
     * @param restDataSourceClient client bound to the PostgREST data source
     */
    public PostgreSQLRestCrudRepository(RestDataSourceClient restDataSourceClient) {
        this.client = restDataSourceClient.getHttpClient().toBlocking();
    }

    @Override
    public <T> T insert(String table, Object row, Class<T> type) {
        return single(client.retrieve(insertRequest(table, row), Argument.listOf(type)));
    }

    @Override
    public <T> Page<T> list(String table, Class<T> type) {
        return list(table, null, type);
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
    public <T> Page<T> list(String table, @Nullable PostgrestQuery query, Class<T> type) {
        HttpResponse<List<T>> response = client.exchange(
                HttpRequest.GET(tableUri(table, query).build()).header(PREFER, COUNT_EXACT),
                Argument.listOf(type));
        List<T> items = response.body() == null ? List.of() : response.body();
        return page(items, query, response.getHeaders().get(HttpHeaders.CONTENT_RANGE));
    }

    @Override
    public int deleteById(String table, String idColumn, Object id) {
        return delete(table, byId(idColumn, id));
    }

    /**
     * Deletes the rows matching a query.
     *
     * @param table table name
     * @param query filters selecting the rows to delete; must not be empty
     * @return the number of deleted rows
     * @throws IllegalArgumentException if the query has no filters
     */
    public int delete(String table, PostgrestQuery query) {
        if (query.filters().isEmpty()) {
            throw new IllegalArgumentException("Refusing to delete from '" + table + "' without filters");
        }
        // PostgREST answers 204 by default; asking for the representation gives us the deleted rows to count.
        MutableHttpRequest<Object> request = HttpRequest.DELETE(tableUri(table, query).build());
        return client.retrieve(request.header(PREFER, RETURN_REPRESENTATION), ROWS).size();
    }

    @Override
    public void close() throws Exception {
        client.close();
    }

    private static PostgrestQuery byId(String idColumn, Object id) {
        return PostgrestQuery.filter(idColumn, EQ + id);
    }

    /**
     * PostgREST always answers with a JSON array unless asked for {@code application/vnd.pgrst.object+json},
     * a media type the Micronaut client cannot decode, so single-row operations unwrap the array here.
     */
    private static <T> T single(List<T> rows) {
        if (rows.isEmpty()) {
            throw new IllegalStateException("PostgREST returned no row");
        }
        return rows.get(0);
    }

    private HttpRequest<?> insertRequest(String table, Object row) {
        return HttpRequest.POST(tableUri(table, null).build(), row).header(PREFER, RETURN_REPRESENTATION);
    }

    /**
     * Parses {@code Content-Range: 0-1/2}, or a range starting with {@code *} when the page is empty.
     * The page number is derived from the offset and the requested limit; without a limit the page is unpaged.
     */
    private static <T> Page<T> page(List<T> items, @Nullable PostgrestQuery query, @Nullable String contentRange) {
        int offset = 0;
        long total = items.size();
        if (contentRange != null) {
            String[] rangeAndTotal = contentRange.split("/");
            if (rangeAndTotal.length == 2 && !"*".equals(rangeAndTotal[1])) {
                total = Long.parseLong(rangeAndTotal[1]);
            }
            if (!rangeAndTotal[0].startsWith("*")) {
                offset = Integer.parseInt(rangeAndTotal[0].split("-")[0]);
            }
        }
        Pageable pageable = query != null && query.limit() != null && query.limit() > 0
            ? Pageable.from(offset / query.limit(), query.limit())
            : Pageable.unpaged();
        return Page.of(items, pageable, total);
    }

    private static UriBuilder tableUri(String table, @Nullable PostgrestQuery query) {
        UriBuilder uri = UriBuilder.of(SLASH).path(table);
        if (query != null) {
            query.filters().forEach(uri::queryParam);
            if (query.select() != null) {
                uri.queryParam(SELECT, query.select());
            }
            if (query.order() != null) {
                uri.queryParam(ORDER, query.order());
            }
            if (query.limit() != null) {
                uri.queryParam(LIMIT, query.limit());
            }
            if (query.offset() != null) {
                uri.queryParam(OFFSET, query.offset());
            }
        }
        return uri;
    }
}
