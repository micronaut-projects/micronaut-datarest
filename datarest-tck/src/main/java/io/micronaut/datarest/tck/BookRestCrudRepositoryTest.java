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
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class BookRestCrudRepositoryTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return RestDatasourceProvider.getFirst().getProperties();
    }

    @Test
    void bookCrud(BookRestCrudRepository repository) {
        String title = "Harry Potter and the Philosopher's Stone";
        String author = "J.K. Rowling";
        String idColumn = "id";
        LocalDate published = LocalDate.of(1997, 6, 26);
        List<Book> books = assertDoesNotThrow(() -> repository.findAll());
        assertEquals(0, books.size());
        Book book = assertDoesNotThrow(() -> repository.save(new Book(null, title, author, published)));
        books = assertDoesNotThrow(() -> repository.findAll());
        assertEquals(1, books.size());
        assertEquals(title, books.getFirst().title());
        assertEquals(author, books.getFirst().author());
        assertEquals(published, books.getFirst().published());

        assertEquals(1, assertDoesNotThrow(() -> repository.count()));
        assertTrue(assertDoesNotThrow(() -> repository.existsById(book.id())));
        assertFalse(assertDoesNotThrow(() -> repository.existsById(-1L)));

        Book found = assertDoesNotThrow(() -> repository.findById(book.id()));
        assertNotNull(found);
        assertEquals(title, found.title());
        assertEquals(published, found.published());
        assertNull(assertDoesNotThrow(() -> repository.findById(-1L)));

        String newTitle = "Harry Potter and the Chamber of Secrets";
        Book updated = assertDoesNotThrow(() -> repository.update(new Book(book.id(), title, book.author(), book.published())));
        assertNotNull(updated);
        assertEquals(newTitle, updated.title());
        assertEquals(author, updated.author());
        assertEquals(published, updated.published());
        assertNull(assertDoesNotThrow(() -> repository.update(new Book(-1L, title, book.author(), book.published()))));

        Book second = assertDoesNotThrow(() -> repository.save(new Book(null, "Dune", "Frank Herbert", null)));
        List<Book> sorted = assertDoesNotThrow(() -> repository.findAll(Sort.of(Sort.Order.desc(idColumn))));
        assertEquals(2, sorted.size());
        assertEquals(second.id(), sorted.get(0).id());
        assertEquals(book.id(), sorted.get(1).id());
        Pageable firstPage = Pageable.from(0, 1, Sort.of(Sort.Order.desc(idColumn)));
        Page<Book> page = assertDoesNotThrow(() -> repository.findAll(firstPage));
        assertEquals(1, page.getNumberOfElements());
        assertEquals(2, page.getTotalSize());
        assertEquals(2, page.getTotalPages());
        assertEquals(second.id(), page.getContent().getFirst().id());
        assertTrue(page.hasNext());
        Page<Book> secondPage = assertDoesNotThrow(() -> repository.findAll(page.nextPageable()));
        assertEquals(1, secondPage.getNumberOfElements());
        assertEquals(book.id(), secondPage.getContent().getFirst().id());
        assertFalse(secondPage.hasNext());

        assertEquals(1, assertDoesNotThrow(() -> repository.deleteById(second.id())));
        int booksDeleted = assertDoesNotThrow(() -> repository.deleteById(book.id()));
        assertEquals(1, booksDeleted);
        assertEquals(0, assertDoesNotThrow(repository::count));
        assertFalse(assertDoesNotThrow(() -> repository.existsById(book.id())));
    }
}
