package io.micronaut.datarest.docs

import io.micronaut.data.model.Page
import io.micronaut.data.model.Sort
import io.micronaut.datarest.core.repositories.RestGenericRepository
import io.micronaut.datarest.core.repositories.postgrest.PostgrestQuery
import io.micronaut.datarest.core.repositories.postgrest.PostgrestRestGenericRepository
import jakarta.inject.Singleton

import java.time.LocalDate

@Singleton
class BookQueries {

    private final PostgrestRestGenericRepository repository

    BookQueries(RestGenericRepository repository) {
        this.repository = (PostgrestRestGenericRepository) repository
    }

    // tag::queries[]
    Page<Book> byAuthor(String author) {
        PostgrestQuery query = PostgrestQuery.filter("author", "eq." + author) // <1>
            .withOrder(Sort.of(Sort.Order.desc("published"))) // <2>
            .withPage(10, 0)
            .withSelect("id,title,author,published") // <3>
        repository.findAll("books", query, Book)
    }

    long countByAuthor(String author) {
        repository.count("books", PostgrestQuery.filter("author", "eq." + author))
    }

    int deletePublishedBefore(LocalDate date) {
        repository.deleteAll("books", PostgrestQuery.filter("published", "lt." + date)) // <4>
    }
    // end::queries[]
}
