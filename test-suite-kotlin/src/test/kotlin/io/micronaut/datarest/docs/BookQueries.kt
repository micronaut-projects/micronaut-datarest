package io.micronaut.datarest.docs

import io.micronaut.data.model.Page
import io.micronaut.data.model.Sort
import io.micronaut.datarest.core.repositories.RestGenericRepository
import io.micronaut.datarest.core.repositories.postgrest.PostgrestQuery
import io.micronaut.datarest.core.repositories.postgrest.PostgrestRestGenericRepository
import jakarta.inject.Singleton
import java.time.LocalDate

@Singleton
class BookQueries(repository: RestGenericRepository) {

    private val repository = repository as PostgrestRestGenericRepository

    // tag::queries[]
    fun byAuthor(author: String): Page<Book> {
        val query = PostgrestQuery.filter("author", "eq.$author") // <1>
            .withOrder(Sort.of(Sort.Order.desc("published"))) // <2>
            .withPage(10, 0)
            .withSelect("id,title,author,published") // <3>
        return repository.findAll("books", query, Book::class.java)
    }

    fun countByAuthor(author: String): Long =
        repository.count("books", PostgrestQuery.filter("author", "eq.$author"))

    fun deletePublishedBefore(date: LocalDate): Int =
        repository.deleteAll("books", PostgrestQuery.filter("published", "lt.$date")) // <4>
    // end::queries[]
}
