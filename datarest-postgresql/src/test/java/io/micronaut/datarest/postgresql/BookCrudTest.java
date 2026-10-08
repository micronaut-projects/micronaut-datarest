package io.micronaut.datarest.postgresql;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.datarest.core.repositories.RestCrudRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class BookCrudTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return PostgreSQL.getProperties();
    }


    @Test
    void bookCrud(RestCrudRepository repository) {
        String title = "Harry Potter and the Philosopher's Stone";
        String author = "J.K. Rowling";
        String table = "books";
        String idColumn = "id";
        Page<Book> books = assertDoesNotThrow(() -> repository.list(table, Book.class));
        assertEquals(0, books.getNumberOfElements());
        Book book = assertDoesNotThrow(() -> repository.insert(table, new BookSave(title, author, null), Book.class));
        books = assertDoesNotThrow(() -> repository.list(table, Book.class));
        assertEquals(1, books.getNumberOfElements());
        assertEquals(title, books.getContent().getFirst().title());
        assertEquals(author, books.getContent().getFirst().author());
        int booksDeleted = assertDoesNotThrow(() -> repository.deleteById(table, idColumn, book.id()));
        assertEquals(1, booksDeleted);
    }
}
