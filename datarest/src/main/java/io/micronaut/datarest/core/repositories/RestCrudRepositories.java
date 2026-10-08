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

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.Internal;
import org.jspecify.annotations.Nullable;

/**
 * Entity-typed operations on top of a {@link RestGenericRepository}, used by the implementations that
 * {@code micronaut-datarest-processor} generates for {@link RestCrudRepository} interfaces. The generic
 * {@code <S extends E>} signatures cannot be expressed in generated source without unchecked casts, so they live here.
 *
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Experimental
@Internal
public final class RestCrudRepositories {

    private RestCrudRepositories() {
    }

    /**
     * Saves an entity and returns the persisted representation, deserialized into the entity's own class.
     *
     * @param repository generic repository of the data source
     * @param table      table name
     * @param entity     entity to insert
     * @param <S>        entity type
     * @return the persisted entity
     */
    @SuppressWarnings("unchecked")
    public static <S> S save(RestGenericRepository repository, String table, S entity) {
        return repository.save(table, entity, (Class<S>) entity.getClass());
    }

    /**
     * Updates the row identified by {@code id} with the entity's columns and returns the updated representation.
     *
     * @param repository generic repository of the data source
     * @param table      table name
     * @param idColumn   identity column name
     * @param id         identity value read from the entity
     * @param entity     entity holding the columns to update
     * @param <S>        entity type
     * @return the updated entity, or {@code null} if no row matches
     * @throws IllegalArgumentException if the entity has no identity
     */
    @SuppressWarnings("unchecked")
    public static <S> @Nullable S update(RestGenericRepository repository, String table, String idColumn, @Nullable Object id, S entity) {
        if (id == null) {
            throw new IllegalArgumentException("Cannot update an entity of table '" + table + "' without an identity");
        }
        return repository.update(table, idColumn, id, entity, (Class<S>) entity.getClass());
    }
}
