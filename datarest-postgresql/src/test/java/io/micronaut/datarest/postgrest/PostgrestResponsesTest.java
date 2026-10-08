package io.micronaut.datarest.postgrest;

import io.micronaut.data.model.Page;
import io.micronaut.data.model.Sort;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.simple.SimpleHttpResponseFactory;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgrestResponsesTest {

    @Test
    void pageKeepsTheQueryOffsetWhenItIsNotAMultipleOfTheLimit() {
        Page<String> page = PostgrestResponses.page(response(List.of("r25", "r26", "r27", "r28", "r29", "r30", "r31", "r32", "r33", "r34"), "25-34/100"),
            PostgrestQuery.page(10, 25).withOrder("published.desc.nullslast,title"));

        assertEquals(25, page.getOffset());
        assertEquals(10, page.getSize());
        assertEquals(100, page.getTotalSize());
        assertTrue(page.hasNext());
        assertEquals(35, page.nextPageable().getOffset());
        assertEquals(25, page.nextPageable().previous().getOffset());
        assertEquals(15, page.getPageable().previous().getOffset());
        assertEquals(Sort.of(Sort.Order.desc("published"), Sort.Order.asc("title")), page.getPageable().getSort());
    }

    @Test
    void emptyPageBeyondTheEndKeepsTheQueryOffset() {
        Page<String> page = PostgrestResponses.page(response(List.of(), "*/40"), PostgrestQuery.page(10, 50));

        assertEquals(50, page.getOffset());
        assertEquals(40, page.getTotalSize());
        assertEquals(0, page.getNumberOfElements());
        assertFalse(page.hasNext());
    }

    @Test
    void lastPageHasNoNext() {
        Page<String> page = PostgrestResponses.page(response(List.of("a", "b"), "8-9/10"), PostgrestQuery.page(5, 8));

        assertEquals(8, page.getOffset());
        assertFalse(page.hasNext());
    }

    @Test
    void queryWithoutLimitIsUnpaged() {
        Page<String> page = PostgrestResponses.page(response(List.of("a"), "0-0/1"), PostgrestQuery.order("id.desc"));

        assertTrue(page.getPageable().isUnpaged());
        assertEquals(Sort.of(Sort.Order.desc("id")), page.getPageable().getSort());
    }

    private static HttpResponse<List<String>> response(List<String> body, String contentRange) {
        return new SimpleHttpResponseFactory().ok(body).header("Content-Range", contentRange);
    }
}
