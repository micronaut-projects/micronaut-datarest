package io.micronaut.datarest.docs;

// tag::class[]
import io.micronaut.datarest.core.annotations.RestRepository;
import io.micronaut.datarest.core.repositories.RestCrudRepository;

@RestRepository // <1>
public interface BookRepository extends RestCrudRepository<Book, Long> { // <2>
}
// end::class[]
