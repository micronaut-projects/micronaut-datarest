/*
 * Copyright 2017-2026 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.datarest.tck;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.datarest.core.repositories.RestCrudRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class BookCrudTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return RestDatasourceProvider.getFirst().getProperties();
    }

    @Test
    void bookCrud(RestCrudRepository repository) {
        String title = "Harry Potter and the Philosopher's Stone";
        String author = "J.K. Rowling";
        String table = "books";
        String idColumn = "id";
        Page<Book> books = assertDoesNotThrow(() -> repository.findAll(table, Book.class));
        assertEquals(0, books.getNumberOfElements());
        Book book = assertDoesNotThrow(() -> repository.save(table, new BookSave(title, author, null), Book.class));
        books = assertDoesNotThrow(() -> repository.findAll(table, Book.class));
        assertEquals(1, books.getNumberOfElements());
        assertEquals(title, books.getContent().getFirst().title());
        assertEquals(author, books.getContent().getFirst().author());

        assertEquals(1, assertDoesNotThrow(() -> repository.count(table)));
        assertTrue(assertDoesNotThrow(() -> repository.existsById(table, idColumn, book.id())));
        assertFalse(assertDoesNotThrow(() -> repository.existsById(table, idColumn, -1L)));

        Book found = assertDoesNotThrow(() -> repository.findById(table, idColumn, book.id(), Book.class));
        assertNotNull(found);
        assertEquals(title, found.title());
        assertNull(assertDoesNotThrow(() -> repository.findById(table, idColumn, -1L, Book.class)));

        String newTitle = "Harry Potter and the Chamber of Secrets";
        Book updated = assertDoesNotThrow(() -> repository.update(table, idColumn, book.id(), Map.of("title", newTitle), Book.class));
        assertNotNull(updated);
        assertEquals(newTitle, updated.title());
        assertEquals(author, updated.author());
        assertNull(assertDoesNotThrow(() -> repository.update(table, idColumn, -1L, Map.of("title", newTitle), Book.class)));

        Book second = assertDoesNotThrow(() -> repository.save(table, new BookSave("Dune", "Frank Herbert", null), Book.class));
        Pageable firstPage = Pageable.from(0, 1, Sort.of(Sort.Order.desc(idColumn)));
        Page<Book> page = assertDoesNotThrow(() -> repository.findAll(table, firstPage, Book.class));
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2, page.getTotalSize());
        assertEquals(2, page.getTotalPages());
        assertEquals(second.id(), page.getContent().getFirst().id());
        assertTrue(page.hasNext());
        Page<Book> secondPage = assertDoesNotThrow(() -> repository.findAll(table, page.nextPageable(), Book.class));
        assertEquals(1, secondPage.getNumberOfElements());
        assertEquals(book.id(), secondPage.getContent().getFirst().id());
        assertFalse(secondPage.hasNext());

        assertEquals(1, assertDoesNotThrow(() -> repository.deleteById(table, idColumn, second.id())));
        int booksDeleted = assertDoesNotThrow(() -> repository.deleteById(table, idColumn, book.id()));
        assertEquals(1, booksDeleted);
        assertEquals(0, assertDoesNotThrow(() -> repository.count(table)));
        assertFalse(assertDoesNotThrow(() -> repository.existsById(table, idColumn, book.id())));
    }
}
