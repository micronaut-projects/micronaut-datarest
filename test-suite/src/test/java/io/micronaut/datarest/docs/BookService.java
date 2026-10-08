package io.micronaut.datarest.docs;

// tag::class[]
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import jakarta.inject.Singleton;

@Singleton
public class BookService {

    private final RestGenericRepository repository;

    BookService(RestGenericRepository repository) { // <1>
        this.repository = repository;
    }

    public Book save(Book book) {
        return repository.save("books", book, Book.class); // <2>
    }

    public Page<Book> findAll(Pageable pageable) {
        return repository.findAll("books", pageable, Book.class);
    }
}
// end::class[]
