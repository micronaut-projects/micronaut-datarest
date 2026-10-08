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

import io.micronaut.core.type.Argument;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.datarest.core.clients.RestDataSourceClient;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import io.micronaut.http.client.BlockingHttpClient;
import org.jspecify.annotations.Nullable;

/**
 * {@link RestGenericRepository} implementation talking to a <a href="https://postgrest.org">PostgREST</a> API.
 * A bean is created for every {@link RestDataSourceClient}.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public final class PostgrestRestGenericRepository implements RestGenericRepository {
    private final BlockingHttpClient client;

    /**
     * @param restDataSourceClient client bound to the PostgREST data source
     */
    public PostgrestRestGenericRepository(RestDataSourceClient restDataSourceClient) {
        this.client = restDataSourceClient.getHttpClient().toBlocking();
    }

    @Override
    public <T> T save(String table, Object row, Class<T> type) {
        return PostgrestResponses.single(client.retrieve(PostgrestRequests.save(table, row), Argument.listOf(type)));
    }

    @Override
    public <T> Page<T> findAll(String table, Class<T> type) {
        return findAll(table, (PostgrestQuery) null, type);
    }

    @Override
    public <T> Page<T> findAll(String table, Pageable pageable, Class<T> type) {
        return PostgrestResponses.page(client.exchange(PostgrestRequests.findAll(table, pageable), Argument.listOf(type)), pageable);
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
    public <T> Page<T> findAll(String table, @Nullable PostgrestQuery query, Class<T> type) {
        return PostgrestResponses.page(client.exchange(PostgrestRequests.findAll(table, query), Argument.listOf(type)), query);
    }

    @Override
    public <T> @Nullable T findById(String table, String idColumn, Object id, Class<T> type) {
        return PostgrestResponses.firstOrNull(client.retrieve(PostgrestRequests.findById(table, idColumn, id), Argument.listOf(type)));
    }

    @Override
    public <T> @Nullable T update(String table, String idColumn, Object id, Object row, Class<T> type) {
        return PostgrestResponses.firstOrNull(client.retrieve(PostgrestRequests.update(table, idColumn, id, row), Argument.listOf(type)));
    }

    @Override
    public long count(String table) {
        return count(table, null);
    }

    /**
     * Counts the rows matching a query without fetching them.
     *
     * @param table table name
     * @param query filters; {@code null} for every row
     * @return the number of matching rows
     */
    public long count(String table, @Nullable PostgrestQuery query) {
        return PostgrestResponses.count(client.exchange(PostgrestRequests.count(table, query)));
    }

    @Override
    public boolean existsById(String table, String idColumn, Object id) {
        return PostgrestResponses.count(client.exchange(PostgrestRequests.existsById(table, idColumn, id))) > 0;
    }

    @Override
    public int deleteById(String table, String idColumn, Object id) {
        return client.retrieve(PostgrestRequests.deleteById(table, idColumn, id), PostgrestResponses.ROWS).size();
    }

    /**
     * Deletes the rows matching a query.
     *
     * @param table table name
     * @param query filters selecting the rows to delete; must not be empty
     * @return the number of deleted rows
     * @throws IllegalArgumentException if the query has no filters
     */
    public int deleteAll(String table, PostgrestQuery query) {
        return client.retrieve(PostgrestRequests.deleteAll(table, query), PostgrestResponses.ROWS).size();
    }
}
