package io.micronaut.datarest.docs;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Sort;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import io.micronaut.datarest.core.repositories.postgrest.PostgrestQuery;
import io.micronaut.datarest.core.repositories.postgrest.PostgrestRestGenericRepository;
import jakarta.inject.Singleton;

import java.time.LocalDate;

@Singleton
public class BookQueries {

    private final PostgrestRestGenericRepository repository;

    BookQueries(RestGenericRepository repository) {
        this.repository = (PostgrestRestGenericRepository) repository;
    }

    // tag::queries[]
    public Page<Book> byAuthor(String author) {
        PostgrestQuery query = PostgrestQuery.filter("author", "eq." + author) // <1>
            .withOrder(Sort.of(Sort.Order.desc("published"))) // <2>
            .withPage(10, 0)
            .withSelect("id,title,author,published"); // <3>
        return repository.findAll("books", query, Book.class);
    }

    public long countByAuthor(String author) {
        return repository.count("books", PostgrestQuery.filter("author", "eq." + author));
    }

    public int deletePublishedBefore(LocalDate date) {
        return repository.deleteAll("books", PostgrestQuery.filter("published", "lt." + date)); // <4>
    }
    // end::queries[]
}
