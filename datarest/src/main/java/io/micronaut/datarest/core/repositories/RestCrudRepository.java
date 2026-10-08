package io.micronaut.datarest.core.repositories;

import io.micronaut.core.annotation.Blocking;

/**
 *
 * @param <E> – The entity type
 * @param <ID> – The ID type
 */
@Blocking
public interface RestCrudRepository<E, ID> {
    <S extends E> S save(S entity);
}
