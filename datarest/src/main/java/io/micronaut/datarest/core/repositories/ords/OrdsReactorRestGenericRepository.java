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
package io.micronaut.datarest.core.repositories.ords;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.ReactorRestGenericRepository;
import io.micronaut.json.JsonMapper;
import reactor.core.publisher.Mono;

/**
 * {@link ReactorRestGenericRepository} implementation talking to an ORDS AutoREST API.
 * A bean is created for every {@link RestDataSourceClient}; it adapts
 * {@link OrdsReactiveRestGenericRepository} to Project Reactor types.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public final class OrdsReactorRestGenericRepository implements ReactorRestGenericRepository {
    private final OrdsReactiveRestGenericRepository reactive;

    /**
     * @param restDataSourceClient client bound to the ORDS data source
     * @param jsonMapper           mapper used to bind rows
     */
    public OrdsReactorRestGenericRepository(RestDataSourceClient restDataSourceClient, JsonMapper jsonMapper) {
        this.reactive = new OrdsReactiveRestGenericRepository(restDataSourceClient, jsonMapper);
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

    @Override
    public Mono<Boolean> existsById(String table, String idColumn, Object id) {
        return Mono.from(reactive.existsById(table, idColumn, id));
    }

    @Override
    public Mono<Integer> deleteById(String table, String idColumn, Object id) {
        return Mono.from(reactive.deleteById(table, idColumn, id));
    }
}
