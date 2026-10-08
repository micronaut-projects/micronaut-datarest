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

/**
 * CRUD operations against a table exposed by a REST data source.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
public interface RestCrudRepository {

    /**
     * Inserts a row and returns the persisted representation.
     *
     * @param table table name
     * @param row   row to insert
     * @param type  type to deserialize the persisted row into
     * @param <T>   row type
     * @return the persisted row
     */
    <T> T insert(String table, Object row, Class<T> type);

    /**
     * Lists the rows of a table.
     *
     * @param table table name
     * @param type  type to deserialize each row into
     * @param <T>   row type
     * @return a page of rows
     */
    <T> Page<T> list(String table, Class<T> type);

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
