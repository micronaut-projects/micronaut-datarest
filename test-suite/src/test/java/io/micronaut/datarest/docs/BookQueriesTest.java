package io.micronaut.datarest.docs;

import io.micronaut.data.model.Page;
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookQueriesTest implements PostgrestTestPropertyProvider {

    @Test
    void filtersOrdersCountsAndDeletes(BookQueries queries, BookRepository repository) {
        Book dune = repository.save(new Book(null, "Dune", "Frank Herbert", LocalDate.of(1965, 8, 1)));
        Book messiah = repository.save(new Book(null, "Dune Messiah", "Frank Herbert", LocalDate.of(1969, 10, 1)));
        repository.save(new Book(null, "Frankenstein", "Mary Shelley", LocalDate.of(1818, 1, 1)));

        Page<Book> herbert = queries.byAuthor("Frank Herbert");
        assertEquals(2, herbert.getNumberOfElements());
        assertEquals(messiah.id(), herbert.getContent().get(0).id());
        assertEquals(dune.id(), herbert.getContent().get(1).id());
        assertEquals(2, queries.countByAuthor("Frank Herbert"));

        assertEquals(1, queries.deletePublishedBefore(LocalDate.of(1900, 1, 1)));
        assertEquals(1, repository.deleteById(dune.id()));
        assertEquals(1, repository.deleteById(messiah.id()));
    }
}
