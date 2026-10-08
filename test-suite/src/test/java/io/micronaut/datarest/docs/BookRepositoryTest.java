package io.micronaut.datarest.docs;

import io.micronaut.data.model.Sort;
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookRepositoryTest implements PostgrestTestPropertyProvider {

    @Test
    void crud(BookRepository repository) {
        Book saved = repository.save(new Book(null, "Dune", "Frank Herbert", LocalDate.of(1965, 8, 1)));
        Long id = saved.id();
        assertNotNull(id);

        Book found = repository.findById(id);
        assertNotNull(found);
        assertEquals("Dune", found.title());
        assertTrue(repository.existsById(id));

        Book updated = repository.update(new Book(id, "Dune Messiah", saved.author(), saved.published()));
        assertNotNull(updated);
        assertEquals("Dune Messiah", updated.title());

        assertTrue(repository.findAll(Sort.of(Sort.Order.desc("id"))).stream().anyMatch(book -> id.equals(book.id())));
        assertTrue(repository.findAll().stream().anyMatch(book -> id.equals(book.id())));
        assertTrue(repository.count() >= 1);

        assertEquals(1, repository.deleteById(id));
        assertFalse(repository.existsById(id));
    }
}
