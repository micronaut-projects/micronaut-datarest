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

import io.micronaut.core.annotation.Blocking;
import io.micronaut.core.annotation.Experimental;
import org.jspecify.annotations.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;

import java.util.List;

/**
 * CRUD operations for an entity mapped to a table of a REST data source. Interfaces extending it and annotated with
 * {@link io.micronaut.datarest.core.annotations.RestRepository} are implemented at compilation time.
 *
 * @param <E>  the entity type, annotated with {@link io.micronaut.data.annotation.MappedEntity}
 * @param <ID> the identity type, the type of the entity property annotated with {@link io.micronaut.data.annotation.Id}
 * @author Sergio del Amo
 * @since 1.0.0
 */
@Experimental
@Blocking
public interface RestCrudRepository<E, ID> {

    /**
     * Finds the entity with the given identity.
     *
     * @param id identity value
     * @return the entity, or {@code null} if none matches
     */
    @Nullable E findById(ID id);

    /**
     * Saves an entity and returns the persisted representation.
     *
     * @param entity entity to insert
     * @param <S>    entity type
     * @return the persisted entity
     */
    <S extends E> S save(S entity);

    /**
     * Updates the row identified by the entity's identity and returns the updated representation.
     *
     * @param entity entity to update
     * @param <S>    entity type
     * @return the updated entity, or {@code null} if no row matches
     */
    <S extends E> @Nullable S update(S entity);

    /**
     * Finds every entity.
     *
     * @return the entities
     */
    List<E> findAll();

    /**
     * Finds every entity in the given order.
     *
     * @param sort sort order
     * @return the entities
     */
    List<E> findAll(Sort sort);

    /**
     * Checks whether an entity with the given identity exists.
     *
     * @param id identity value
     * @return {@code true} if it exists
     */
    boolean existsById(ID id);

    /**
     * Counts the entities.
     *
     * @return the number of entities
     */
    long count();

    /**
     * Deletes the entity with the given identity.
     *
     * @param id identity value
     * @return the number of deleted rows
     */
    int deleteById(ID id);

    /**
     * Finds a page of entities.
     *
     * @param pageable page request
     * @return the page
     */
    Page<E> findAll(Pageable pageable);
}
