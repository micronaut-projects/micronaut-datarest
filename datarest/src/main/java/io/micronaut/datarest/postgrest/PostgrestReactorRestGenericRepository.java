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

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.ReactorRestGenericRepository;
import org.jspecify.annotations.Nullable;
import reactor.core.publisher.Mono;

/**
 * {@link ReactorRestGenericRepository} implementation talking to a <a href="https://postgrest.org">PostgREST</a> API.
 * A bean is created for every {@link RestDataSourceClient}; it adapts
 * {@link PostgrestReactiveRestGenericRepository} to Project Reactor types.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public final class PostgrestReactorRestGenericRepository implements ReactorRestGenericRepository {
    private final PostgrestReactiveRestGenericRepository reactive;

    /**
     * @param restDataSourceClient client bound to the PostgREST data source
     */
    public PostgrestReactorRestGenericRepository(RestDataSourceClient restDataSourceClient) {
        this.reactive = new PostgrestReactiveRestGenericRepository(restDataSourceClient);
    }

    @Override
    public <T> Mono<T> save(String table, Object row, Class<T> type) {
        return Mono.from(reactive.save(table, row, type));
    }

    @Override
    public <T> Mono<Page<T>> findAll(String table, Class<T> type) {
        return Mono.from(reactive.findAll(table, type));
    }

    @Override
    public <T> Mono<Page<T>> findAll(String table, Pageable pageable, Class<T> type) {
        return Mono.from(reactive.findAll(table, pageable, type));
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
    public <T> Mono<Page<T>> findAll(String table, @Nullable PostgrestQuery query, Class<T> type) {
        return Mono.from(reactive.findAll(table, query, type));
    }

    @Override
    public <T> Mono<T> findById(String table, String idColumn, Object id, Class<T> type) {
        return Mono.from(reactive.findById(table, idColumn, id, type));
    }

    @Override
    public <T> Mono<T> update(String table, String idColumn, Object id, Object row, Class<T> type) {
        return Mono.from(reactive.update(table, idColumn, id, row, type));
    }

    @Override
    public Mono<Long> count(String table) {
        return Mono.from(reactive.count(table));
    }

    /**
     * Counts the rows matching a query without fetching them.
     *
     * @param table table name
     * @param query filters; {@code null} for every row
     * @return the number of matching rows
     */
    public Mono<Long> count(String table, @Nullable PostgrestQuery query) {
        return Mono.from(reactive.count(table, query));
    }

    @Override
    public Mono<Boolean> existsById(String table, String idColumn, Object id) {
        return Mono.from(reactive.existsById(table, idColumn, id));
    }

    @Override
    public Mono<Integer> deleteById(String table, String idColumn, Object id) {
        return Mono.from(reactive.deleteById(table, idColumn, id));
    }

    /**
     * Deletes the rows matching a query.
     *
     * @param table table name
     * @param query filters selecting the rows to delete; must not be empty
     * @return the number of deleted rows
     * @throws IllegalArgumentException if the query has no filters
     */
    public Mono<Integer> deleteAll(String table, PostgrestQuery query) {
        return Mono.from(reactive.deleteAll(table, query));
    }
}
