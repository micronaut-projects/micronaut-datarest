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
import io.micronaut.datarest.core.repositories.ReactiveRestCrudRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class BookReactiveCrudTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return RestDatasourceProvider.getFirst().getProperties();
    }

    @Test
    void bookCrud(ReactiveRestCrudRepository repository) {
        String title = "Harry Potter and the Philosopher's Stone";
        String author = "J.K. Rowling";
        String table = "books";
        String idColumn = "id";
        Page<Book> books = assertDoesNotThrow(() -> Mono.from(repository.findAll(table, Book.class)).block());
        assertEquals(0, books.getNumberOfElements());
        Book book = assertDoesNotThrow(() -> Mono.from(repository.save(table, new BookSave(title, author, null), Book.class)).block());
        books = assertDoesNotThrow(() -> Mono.from(repository.findAll(table, Book.class)).block());
        assertEquals(1, books.getNumberOfElements());
        assertEquals(title, books.getContent().getFirst().title());
        assertEquals(author, books.getContent().getFirst().author());

        assertEquals(1, assertDoesNotThrow(() -> Mono.from(repository.count(table)).block()));
        assertTrue(assertDoesNotThrow(() -> Mono.from(repository.existsById(table, idColumn, book.id())).block()));
        assertFalse(assertDoesNotThrow(() -> Mono.from(repository.existsById(table, idColumn, -1L)).block()));

        Book found = assertDoesNotThrow(() -> Mono.from(repository.findById(table, idColumn, book.id(), Book.class)).block());
        assertNotNull(found);
        assertEquals(title, found.title());
        assertNull(assertDoesNotThrow(() -> Mono.from(repository.findById(table, idColumn, -1L, Book.class)).block()));

        String newTitle = "Harry Potter and the Chamber of Secrets";
        Book updated = assertDoesNotThrow(() -> Mono.from(repository.update(table, idColumn, book.id(), Map.of("title", newTitle), Book.class)).block());
        assertNotNull(updated);
        assertEquals(newTitle, updated.title());
        assertEquals(author, updated.author());
        assertNull(assertDoesNotThrow(() -> Mono.from(repository.update(table, idColumn, -1L, Map.of("title", newTitle), Book.class)).block()));

        Book second = assertDoesNotThrow(() -> Mono.from(repository.save(table, new BookSave("Dune", "Frank Herbert", null), Book.class)).block());
        Pageable firstPage = Pageable.from(0, 1, Sort.of(Sort.Order.desc(idColumn)));
        Page<Book> page = assertDoesNotThrow(() -> Mono.from(repository.findAll(table, firstPage, Book.class)).block());
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2, page.getTotalSize());
        assertEquals(2, page.getTotalPages());
        assertEquals(second.id(), page.getContent().getFirst().id());
        assertTrue(page.hasNext());
        Page<Book> secondPage = assertDoesNotThrow(() -> Mono.from(repository.findAll(table, page.nextPageable(), Book.class)).block());
        assertEquals(1, secondPage.getNumberOfElements());
        assertEquals(book.id(), secondPage.getContent().getFirst().id());
        assertFalse(secondPage.hasNext());

        assertEquals(1, assertDoesNotThrow(() -> Mono.from(repository.deleteById(table, idColumn, second.id())).block()));
        int booksDeleted = assertDoesNotThrow(() -> Mono.from(repository.deleteById(table, idColumn, book.id())).block());
        assertEquals(1, booksDeleted);
        assertEquals(0, assertDoesNotThrow(() -> Mono.from(repository.count(table)).block()));
        assertFalse(assertDoesNotThrow(() -> Mono.from(repository.existsById(table, idColumn, book.id())).block()));
    }
}
