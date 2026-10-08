package io.micronaut.datarest.tck;

import io.micronaut.datarest.core.annotations.RestRepository;
import io.micronaut.datarest.core.repositories.RestCrudRepository;

@RestRepository
public interface BookRestCrudRepository extends RestCrudRepository<Book, Long> {
}
