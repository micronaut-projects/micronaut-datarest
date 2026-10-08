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
package io.micronaut.datarest.core.repositories;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import org.jspecify.annotations.Nullable;

/**
 * CRUD operations against a table exposed by a REST data source.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public interface RestGenericRepository {

    /**
     * Saves a row and returns the persisted representation.
     *
     * @param table table name
     * @param row   row to insert
     * @param type  type to deserialize the persisted row into
     * @param <T>   row type
     * @return the persisted row
     */
    <T> T save(String table, Object row, Class<T> type);

    /**
     * Lists the rows of a table.
     *
     * @param table table name
     * @param type  type to deserialize each row into
     * @param <T>   row type
     * @return a page of rows
     */
    <T> Page<T> findAll(String table, Class<T> type);

    /**
     * Lists a page of rows of a table, honouring the page size, offset and sort of the {@link Pageable}.
     *
     * @param table    table name
     * @param pageable page request
     * @param type     type to deserialize each row into
     * @param <T>      row type
     * @return a page of rows
     */
    <T> Page<T> findAll(String table, Pageable pageable, Class<T> type);

    /**
     * Lists every row of a table in the given order. Equivalent to {@code findAll(table, Pageable.from(sort), type)}.
     *
     * @param table table name
     * @param sort  sort order
     * @param type  type to deserialize each row into
     * @param <T>   row type
     * @return a page holding every row
     */
    default <T> Page<T> findAll(String table, Sort sort, Class<T> type) {
        return findAll(table, Pageable.from(sort), type);
    }

    /**
     * Finds the row whose identifier column matches the given value.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @param type     type to deserialize the row into
     * @param <T>      row type
     * @return the row, or {@code null} if none matches
     */
    <T> @Nullable T findById(String table, String idColumn, Object id, Class<T> type);

    /**
     * Updates the row whose identifier column matches the given value and returns the updated representation.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @param row      columns to update
     * @param type     type to deserialize the updated row into
     * @param <T>      row type
     * @return the updated row, or {@code null} if no row matches
     */
    <T> @Nullable T update(String table, String idColumn, Object id, Object row, Class<T> type);

    /**
     * Counts the rows of a table.
     *
     * @param table table name
     * @return the number of rows
     */
    long count(String table);

    /**
     * Checks whether a row whose identifier column matches the given value exists.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @return {@code true} if the row exists
     */
    boolean existsById(String table, String idColumn, Object id);

    /**
     * Deletes the row whose identifier column matches the given value.
     *
     * @param table    table name
     * @param idColumn identifier column name
     * @param id       identifier value
     * @return the number of deleted rows
     */
    int deleteById(String table, String idColumn, Object id);
}
