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

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Query parameters accepted by a PostgREST table endpoint.
 *
 * @param filters column name to PostgREST operator expression, for example {@code title -> like.D*} or
 *                {@code id -> in.(1,2)}; sent as {@code column=expression} query parameters
 * @param order   PostgREST order expression such as {@code title.asc} or {@code published.desc.nullslast}
 * @param limit   maximum number of rows; {@code null} for the server default
 * @param offset  number of rows to skip; {@code null} for 0
 * @param select  columns to return, for example {@code id,title}; {@code null} for every column
 */
public record PostgrestQuery(@NonNull Map<String, String> filters,
                             @Nullable String order,
                             @Nullable Integer limit,
                             @Nullable Integer offset,
                             @Nullable String select) {

    public PostgrestQuery {
        filters = Collections.unmodifiableMap(new LinkedHashMap<>(filters));
    }

    @NonNull
    public static PostgrestQuery none() {
        return new PostgrestQuery(Map.of(), null, null, null, null);
    }

    @NonNull
    public static PostgrestQuery filter(@NonNull String column, @NonNull String expression) {
        return none().withFilter(column, expression);
    }

    @NonNull
    public static PostgrestQuery order(@NonNull String order) {
        return none().withOrder(order);
    }

    @NonNull
    public static PostgrestQuery page(int limit, int offset) {
        return none().withPage(limit, offset);
    }

    @NonNull
    public PostgrestQuery withFilter(@NonNull String column, @NonNull String expression) {
        Map<String, String> copy = new LinkedHashMap<>(filters);
        copy.put(column, expression);
        return new PostgrestQuery(copy, order, limit, offset, select);
    }

    @NonNull
    public PostgrestQuery withOrder(@Nullable String order) {
        return new PostgrestQuery(filters, order, limit, offset, select);
    }

    @NonNull
    public PostgrestQuery withPage(int limit, int offset) {
        return new PostgrestQuery(filters, order, limit, offset, select);
    }

    @NonNull
    public PostgrestQuery withSelect(@Nullable String select) {
        return new PostgrestQuery(filters, order, limit, offset, select);
    }
}
