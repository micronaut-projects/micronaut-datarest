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
package io.micronaut.datarest.postgrest;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Turns <a href="https://postgrest.org">PostgREST</a> responses into rows, pages and counts.
 * Shared by the blocking and reactive repositories.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Internal
public final class PostgrestResponses {
    /**
     * Body type of a response listing rows whose shape is irrelevant, such as deleted rows.
     */
    public static final Argument<List<Map<String, Object>>> ROWS =
            Argument.listOf(Argument.mapOf(String.class, Object.class));
    private static final String SLASH = "/";
    private static final String COMMA = ",";
    private static final String DOT = "\\.";
    private static final String DESC = "desc";
    private static final String DASH = "-";
    private static final String UNKNOWN = "*";

    private PostgrestResponses() {
    }

    /**
     * @param response a response listing rows
     * @param <T>      row type
     * @return the rows, or an empty list when the body is absent
     */
    public static <T> List<T> items(HttpResponse<List<T>> response) {
        List<T> body = response.body();
        return body == null ? List.of() : body;
    }

    /**
     * PostgREST always answers with a JSON array unless asked for {@code application/vnd.pgrst.object+json},
     * a media type the Micronaut client cannot decode, so single-row operations unwrap the array here.
     *
     * @param rows rows of the response
     * @param <T>  row type
     * @return the first row, or {@code null} when there is none
     */
    public static <T> @Nullable T firstOrNull(List<T> rows) {
        return rows.isEmpty() ? null : rows.getFirst();
    }

    /**
     * @param rows rows of the response
     * @param <T>  row type
     * @return the first row
     * @throws IllegalStateException when there is none
     */
    public static <T> T single(List<T> rows) {
        if (rows.isEmpty()) {
            throw new IllegalStateException("PostgREST returned no row");
        }
        return rows.getFirst();
    }

    /**
     * Builds a page for a {@link Pageable} request, keeping its sort so that {@link Page#nextPageable()} works.
     *
     * @param response response of {@link PostgrestRequests#findAll(String, Pageable)}
     * @param pageable the page request
     * @param <T>      row type
     * @return the page
     */
    public static <T> Page<T> page(HttpResponse<List<T>> response, Pageable pageable) {
        List<T> items = items(response);
        return Page.of(items, pageable, total(contentRange(response), items.size()));
    }

    /**
     * Builds a page for a {@link PostgrestQuery} request. The page keeps the query's own offset and limit,
     * so {@link Page#nextPageable()} continues exactly where the query stopped, and its sort as far as the
     * {@code order} value can be read back ({@code column.asc,other.desc}; PostgREST only modifiers such as
     * {@code nullslast} are dropped). Without a limit the page is unpaged. The total comes from
     * {@code Content-Range}.
     *
     * @param response response of {@link PostgrestRequests#findAll(String, PostgrestQuery)}
     * @param query    the query, or {@code null} when every row was requested
     * @param <T>      row type
     * @return the page
     */
    public static <T> Page<T> page(HttpResponse<List<T>> response, @Nullable PostgrestQuery query) {
        List<T> items = items(response);
        return Page.of(items, pageable(query), total(contentRange(response), items.size()));
    }

    private static Pageable pageable(@Nullable PostgrestQuery query) {
        if (query == null) {
            return Pageable.unpaged();
        }
        Sort sort = sort(query.order());
        if (query.limit() == null || query.limit() <= 0) {
            return Pageable.from(sort);
        }
        long offset = query.offset() == null ? 0 : query.offset();
        return new OffsetPageable(offset, query.limit(), sort);
    }

    /**
     * Reads a PostgREST {@code order} value back into a {@link Sort}: {@code col}, {@code col.asc} or
     * {@code col.desc} per comma separated item; further modifiers are ignored.
     */
    private static Sort sort(@Nullable String order) {
        if (order == null || order.isBlank()) {
            return Sort.unsorted();
        }
        List<Sort.Order> orders = new ArrayList<>();
        for (String item : order.split(COMMA)) {
            String[] parts = item.trim().split(DOT);
            if (!parts[0].isEmpty()) {
                boolean descending = parts.length > 1 && DESC.equalsIgnoreCase(parts[1]);
                orders.add(descending ? Sort.Order.desc(parts[0]) : Sort.Order.asc(parts[0]));
            }
        }
        return Sort.of(orders);
    }

    /**
     * @param response response of {@link PostgrestRequests#count(String, PostgrestQuery)}
     * @return the exact count from {@code Content-Range}, or 0 when absent
     */
    public static long count(HttpResponse<?> response) {
        return total(contentRange(response), 0);
    }

    /**
     * Parses the total after the slash of {@code Content-Range: 0-1/2}.
     *
     * @param contentRange the header value
     * @param fallback     value when the header is absent or the total is unknown ({@code *})
     * @return the total
     */
    public static long total(@Nullable String contentRange, long fallback) {
        if (contentRange == null) {
            return fallback;
        }
        String[] rangeAndTotal = contentRange.split(SLASH);
        if (rangeAndTotal.length == 2 && !UNKNOWN.equals(rangeAndTotal[1])) {
            return Long.parseLong(rangeAndTotal[1]);
        }
        return fallback;
    }

    /**
     * Parses the first row index of {@code Content-Range: 0-1/2}.
     *
     * @param contentRange the header value
     * @return the offset, or 0 when the header is absent or the range is empty ({@code *})
     */
    public static int offset(@Nullable String contentRange) {
        if (contentRange == null) {
            return 0;
        }
        String range = contentRange.split(SLASH)[0];
        return range.startsWith(UNKNOWN) ? 0 : Integer.parseInt(range.split(DASH)[0]);
    }

    private static @Nullable String contentRange(HttpResponse<?> response) {
        return response.getHeaders().get(HttpHeaders.CONTENT_RANGE);
    }
}
