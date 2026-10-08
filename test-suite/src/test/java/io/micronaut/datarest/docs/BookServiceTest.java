package io.micronaut.datarest.docs;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BookServiceTest implements PostgrestTestPropertyProvider {

    @Test
    void savesAndPages(BookService service, BookRepository repository) {
        Book saved = service.save(new Book(null, "Neuromancer", "William Gibson", null));
        assertNotNull(saved.id());

        Page<Book> page = service.findAll(Pageable.from(0, 1, Sort.of(Sort.Order.desc("id"))));
        assertEquals(1, page.getNumberOfElements());
        assertEquals(saved.id(), page.getContent().getFirst().id());

        assertEquals(1, repository.deleteById(saved.id()));
    }
}
