package io.micronaut.datarest.ords;

import io.micronaut.datarest.core.annotations.RestRepository;
import io.micronaut.datarest.core.repositories.RestCrudRepository;
import io.micronaut.datarest.tck.Book;

@RestRepository
public interface BookRestCrudRepository extends RestCrudRepository<Book, String> {
}
