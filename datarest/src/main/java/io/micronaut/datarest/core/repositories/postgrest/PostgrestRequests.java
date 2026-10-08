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
package io.micronaut.datarest.core.repositories.postgrest;

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.uri.UriBuilder;
import org.jspecify.annotations.Nullable;


/**
 * Builds the HTTP requests of a <a href="https://postgrest.org">PostgREST</a> API.
 * Shared by the blocking and reactive repositories, which only differ in how they send them.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
public final class PostgrestRequests {
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

    private PostgrestRequests() {
    }

    /**
     * {@code POST /table} returning the persisted row.
     *
     * @param table table name
     * @param row   row to insert
     * @return the request
     */
    public static MutableHttpRequest<Object> save(String table, Object row) {
        return HttpRequest.POST(tableUri(table, null).build(), row).header(PREFER, RETURN_REPRESENTATION);
    }

    /**
     * {@code GET /table} with an exact count in {@code Content-Range}.
     *
     * @param table table name
     * @param query filters, ordering and pagination; {@code null} for every row
     * @return the request
     */
    public static MutableHttpRequest<Object> findAll(String table, @Nullable PostgrestQuery query) {
        return HttpRequest.GET(tableUri(table, query).build()).header(PREFER, COUNT_EXACT);
    }

    /**
     * {@code GET /table} honouring the page size, offset and sort of a {@link Pageable}.
     *
     * @param table    table name
     * @param pageable page request
     * @return the request
     */
    public static MutableHttpRequest<Object> findAll(String table, Pageable pageable) {
        return findAll(table, toQuery(pageable));
    }

    /**
     * {@code GET /table?idColumn=eq.id}.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @return the request
     */
    public static MutableHttpRequest<Object> findById(String table, String idColumn, Object id) {
        return HttpRequest.GET(tableUri(table, byId(idColumn, id)).build());
    }

    /**
     * {@code PATCH /table?idColumn=eq.id} returning the updated row.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @param row      columns to update
     * @return the request
     */
    public static MutableHttpRequest<Object> update(String table, String idColumn, Object id, Object row) {
        return HttpRequest.PATCH(tableUri(table, byId(idColumn, id)).build(), row).header(PREFER, RETURN_REPRESENTATION);
    }

    /**
     * {@code HEAD /table} with an exact count in {@code Content-Range} and no body.
     *
     * @param table table name
     * @param query filters; {@code null} for every row
     * @return the request
     */
    public static MutableHttpRequest<?> count(String table, @Nullable PostgrestQuery query) {
        return HttpRequest.HEAD(tableUri(table, query).build()).header(PREFER, COUNT_EXACT);
    }

    /**
     * {@code HEAD /table?idColumn=eq.id} with an exact count in {@code Content-Range}.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @return the request
     */
    public static MutableHttpRequest<?> existsById(String table, String idColumn, Object id) {
        return count(table, byId(idColumn, id));
    }

    /**
     * {@code DELETE /table} returning the deleted rows, so callers can count them.
     *
     * @param table table name
     * @param query filters selecting the rows to delete; must not be empty
     * @return the request
     * @throws IllegalArgumentException if the query has no filters
     */
    public static MutableHttpRequest<Object> deleteAll(String table, PostgrestQuery query) {
        if (query.filters().isEmpty()) {
            throw new IllegalArgumentException("Refusing to delete from '" + table + "' without filters");
        }
        return HttpRequest.DELETE(tableUri(table, query).build()).header(PREFER, RETURN_REPRESENTATION);
    }

    /**
     * {@code DELETE /table?idColumn=eq.id} returning the deleted rows.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @return the request
     */
    public static MutableHttpRequest<Object> deleteById(String table, String idColumn, Object id) {
        return deleteAll(table, byId(idColumn, id));
    }

    /**
     * Maps a {@link Pageable} to PostgREST {@code limit}, {@code offset} and {@code order} parameters.
     *
     * @param pageable page request
     * @return the equivalent query
     */
    public static PostgrestQuery toQuery(Pageable pageable) {
        PostgrestQuery query = PostgrestQuery.none();
        if (!pageable.isUnpaged()) {
            query = query.withPage(pageable.getSize(), (int) pageable.getOffset());
        }
        if (pageable.isSorted()) {
            query = query.withOrder(pageable.getSort());
        }
        return query;
    }

    private static PostgrestQuery byId(String idColumn, Object id) {
        return PostgrestQuery.filter(idColumn, EQ + id);
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
