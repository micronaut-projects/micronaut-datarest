package io.micronaut.datarest.postgrest;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Sort;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
import io.micronaut.datarest.core.repositories.postgrest.PostgrestQuery;
import io.micronaut.datarest.core.repositories.postgrest.PostgrestRestGenericRepository;
import io.micronaut.datarest.tck.Book;
import io.micronaut.datarest.tck.BookSave;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pages built from a {@link PostgrestQuery} continue from the query's own offset, even when it is not a
 * multiple of the limit.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class PostgreSQLQueryPagingTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return PostgreSQL.getProperties();
    }

    @Test
    void queryPagesContinueFromTheirOffset(RestGenericRepository genericRepository) {
        PostgrestRestGenericRepository repository = assertInstanceOf(PostgrestRestGenericRepository.class, genericRepository);
        String table = "books";
        List<Long> ids = List.of("A", "B", "C", "D", "E").stream()
            .map(title -> repository.save(table, new BookSave(title, "author", null), Book.class).id())
            .toList();
        try {
            PostgrestQuery query = PostgrestQuery.page(2, 1).withOrder(Sort.of(Sort.Order.asc("id")));
            Page<Book> page = repository.findAll(table, query, Book.class);
            assertEquals(1, page.getOffset());
            assertEquals(5, page.getTotalSize());
            assertEquals(List.of("B", "C"), page.getContent().stream().map(Book::title).toList());
            assertTrue(page.hasNext());

            Page<Book> next = repository.findAll(table, page.nextPageable(), Book.class);
            assertEquals(3, next.getOffset());
            assertEquals(List.of("D", "E"), next.getContent().stream().map(Book::title).toList());
            assertFalse(next.hasNext());
        } finally {
            assertEquals(ids.size(), repository.deleteAll(table, PostgrestQuery.filter("id", "gte." + ids.getFirst())));
        }
    }
}
