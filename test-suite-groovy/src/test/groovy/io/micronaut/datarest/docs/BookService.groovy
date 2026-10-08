package io.micronaut.datarest.docs

// tag::class[]
import io.micronaut.data.model.Page
import io.micronaut.data.model.Pageable
import io.micronaut.datarest.core.repositories.RestGenericRepository
import jakarta.inject.Singleton

@Singleton
class BookService {

    private final RestGenericRepository repository

    BookService(RestGenericRepository repository) { // <1>
        this.repository = repository
    }

    Book save(Book book) {
        repository.save("books", book, Book) // <2>
    }

    Page<Book> findAll(Pageable pageable) {
        repository.findAll("books", pageable, Book)
    }
}
// end::class[]
