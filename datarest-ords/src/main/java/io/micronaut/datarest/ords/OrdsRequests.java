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

import io.micronaut.core.annotation.Internal;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.json.JsonMapper;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Builds the HTTP requests of an
 * <a href="https://docs.oracle.com/en/database/oracle/oracle-rest-data-services/">ORDS AutoREST</a> API.
 * Request URIs are relative to the data source URL, which ends with the schema alias, for example
 * {@code http://localhost:8080/ords/hr}; the data source's client prepends that path.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
public final class OrdsRequests {
    /**
     * Query parameter limiting the number of rows.
     */
    public static final String LIMIT = "limit";
    /**
     * Query parameter skipping rows.
     */
    public static final String OFFSET = "offset";
    /**
     * Query parameter holding the JSON filter and ordering document.
     */
    public static final String Q = "q";
    private static final String SLASH = "/";
    private static final String ORDER_BY = "$orderby";
    private static final String ASC = "ASC";
    private static final String DESC = "DESC";
    private final JsonMapper jsonMapper;

    /**
     * @param jsonMapper mapper used to render the {@code q} query document
     */
    public OrdsRequests(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    /**
     * {@code POST /table/} returning the inserted row.
     *
     * @param table table alias
     * @param row   row to insert
     * @return the request
     */
    public MutableHttpRequest<Object> save(String table, Object row) {
        return HttpRequest.POST(collection(table).build(), row);
    }

    /**
     * {@code GET /table/} with optional {@code limit}, {@code offset} and {@code q} parameters.
     *
     * @param table    table alias
     * @param pageable page request; {@code null} for the server default page
     * @return the request
     */
    public MutableHttpRequest<Object> findAll(String table, @Nullable Pageable pageable) {
        UriBuilder uri = collection(table);
        if (pageable != null) {
            if (!pageable.isUnpaged()) {
                uri.queryParam(LIMIT, pageable.getSize()).queryParam(OFFSET, pageable.getOffset());
            }
            if (pageable.isSorted()) {
                uri.queryParam(Q, orderBy(pageable.getSort()));
            }
        }
        return HttpRequest.GET(uri.build());
    }

    /**
     * {@code GET /table/} fetching a window of rows, used to count.
     *
     * @param table  table alias
     * @param limit  maximum number of rows
     * @param offset number of rows to skip
     * @return the request
     */
    public MutableHttpRequest<Object> findAll(String table, int limit, long offset) {
        return HttpRequest.GET(collection(table).queryParam(LIMIT, limit).queryParam(OFFSET, offset).build());
    }

    /**
     * {@code GET /table/id}.
     *
     * @param table table alias
     * @param id    primary key value
     * @return the request
     */
    public MutableHttpRequest<Object> findById(String table, Object id) {
        return HttpRequest.GET(row(table, id));
    }

    /**
     * {@code PUT /table/id} replacing the row.
     *
     * @param table table alias
     * @param id    primary key value
     * @param row   the full row
     * @return the request
     */
    public MutableHttpRequest<Object> update(String table, Object id, Object row) {
        return HttpRequest.PUT(row(table, id), row);
    }

    /**
     * {@code DELETE /table/id}.
     *
     * @param table table alias
     * @param id    primary key value
     * @return the request
     */
    public MutableHttpRequest<Object> deleteById(String table, Object id) {
        return HttpRequest.DELETE(row(table, id));
    }

    /**
     * {@code /table/}: AutoREST collections end with a slash, otherwise ORDS redirects.
     */
    private static UriBuilder collection(String table) {
        return UriBuilder.of(SLASH).path(table).path(SLASH);
    }

    private static URI row(String table, Object id) {
        return UriBuilder.of(SLASH).path(table).path(String.valueOf(id)).build();
    }

    /**
     * Renders a {@link Sort} as the ORDS {@code {"$orderby":{"column":"ASC"}}} query document.
     */
    private String orderBy(Sort sort) {
        Map<String, String> columns = new LinkedHashMap<>();
        for (Sort.Order order : sort.getOrderBy()) {
            columns.put(order.getProperty(), order.isAscending() ? ASC : DESC);
        }
        try {
            return jsonMapper.writeValueAsString(Map.of(ORDER_BY, columns));
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not render the ORDS order by document", e);
        }
    }
}
