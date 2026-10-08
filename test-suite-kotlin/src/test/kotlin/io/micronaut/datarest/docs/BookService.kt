package io.micronaut.datarest.docs

// tag::class[]
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.datarest.core.repositories.RestGenericRepository
import jakarta.inject.Singleton

@Singleton
class BookService(private val repository: RestGenericRepository) { // <1>

    fun save(book: Book): Book = repository.save("books", book, Book::class.java) // <2>

    fun findAll(pageable: Pageable): Page<Book> = repository.findAll("books", pageable, Book::class.java)
}
// end::class[]
