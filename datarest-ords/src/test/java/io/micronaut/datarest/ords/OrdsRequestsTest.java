package io.micronaut.datarest.ords;

import io.micronaut.data.model.Pageable;
import io.micronaut.data.model.Sort;
import io.micronaut.json.JsonMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false)
class OrdsRequestsTest {

    @Inject
    JsonMapper jsonMapper;

    @Test
    void requestsAreRelativeToTheDataSourceUrl() {
        OrdsRequests requests = new OrdsRequests(jsonMapper);
        assertEquals("/books/", requests.findAll("books", null).getUri().toString());
        assertEquals("/books/?limit=10&offset=20", requests.findAll("books", Pageable.from(2, 10)).getUri().toString());
        assertEquals("/books/?q=%7B%22%24orderby%22%3A%7B%22id%22%3A%22DESC%22%7D%7D",
            requests.findAll("books", Pageable.from(Sort.of(Sort.Order.desc("id")))).getUri().toString());
        assertEquals("/books/1", requests.findById("books", 1).getUri().toString());
        assertEquals("/books/1", requests.deleteById("books", 1).getUri().toString());
    }

    @Test
    void tableAliasesAndKeysArePercentEncodedPerSegment() {
        OrdsRequests requests = new OrdsRequests(jsonMapper);
        assertEquals("/books/a%20b", requests.findById("books", "a b").getUri().toString());
        assertEquals("/books/a%2Fb", requests.findById("books", "a/b").getUri().toString());
        assertEquals("/books/a%3Fb%23c", requests.deleteById("books", "a?b#c").getUri().toString());
        assertEquals("/books/100%25", requests.findById("books", "100%").getUri().toString());
        assertEquals("/books/caf%C3%A9", requests.findById("books", "caf\u00e9").getUri().toString());
        assertEquals("/my%20table/", requests.findAll("my table", null).getUri().toString());
        assertEquals("/books/ISBN-978.0_1~", requests.findById("books", "ISBN-978.0_1~").getUri().toString());
    }
}
