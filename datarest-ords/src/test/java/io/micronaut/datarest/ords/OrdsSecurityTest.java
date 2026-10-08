package io.micronaut.datarest.ords;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proves the fixture is secured: a raw client without a token is refused by the protected AutoREST object.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class OrdsSecurityTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return Ords.getProperties();
    }

    @Test
    void anonymousRequestsAreRefused() throws Exception {
        try (HttpClient raw = HttpClient.create(new URL(Ords.getUrl()))) {
            HttpClientResponseException e = assertThrows(HttpClientResponseException.class,
                () -> raw.toBlocking().exchange(HttpRequest.GET(Ords.getUrl() + "/books/")));
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        }
    }
}
