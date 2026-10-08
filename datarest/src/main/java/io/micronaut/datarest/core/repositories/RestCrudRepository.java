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
import io.micronaut.core.annotation.Nullable;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import java.util.List;

/**
 *
 * @param <E> – The entity type
 * @param <ID> – The ID type
 */
@Blocking
public interface RestCrudRepository<E, ID> {
    @Nullable E findById(ID id);

    <S extends E> S save(S entity);

    <S extends E> S update(S entity);

    List<E> findAll();

    List<E> findAll(Sort sort);

    boolean existsById(ID id);

    long count();

    int deleteById(ID id);

    /**
     * Finds all records for the given pageable.
     *
     * @param pageable The pageable.
     * @return The results
     */
    Page<E> findAll(Pageable pageable);
}
