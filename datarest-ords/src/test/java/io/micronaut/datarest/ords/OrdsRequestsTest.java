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
}
