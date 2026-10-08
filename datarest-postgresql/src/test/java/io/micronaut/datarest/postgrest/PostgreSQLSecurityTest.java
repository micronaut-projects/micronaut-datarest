package io.micronaut.datarest.postgrest;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Proves the fixture is secured: a raw client without the token is refused, while the data source's
 * registry managed client, which the {@link PostgreSQLBearerTokenFilter} applies to, is accepted.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class PostgreSQLSecurityTest implements TestPropertyProvider {

    @Override
    public @NonNull Map<String, String> getProperties() {
        return PostgreSQL.getProperties();
    }

    @Test
    void anonymousRequestsAreRefused() throws Exception {
        try (HttpClient raw = rawClient()) {
            HttpClientResponseException e = assertThrows(HttpClientResponseException.class,
                () -> raw.toBlocking().exchange(HttpRequest.GET("/books")));
            assertEquals(HttpStatus.UNAUTHORIZED, e.getStatus());
        }
    }

    @Test
    void tokenRequestsAreAccepted() throws Exception {
        try (HttpClient raw = rawClient()) {
            assertEquals(HttpStatus.OK,
                raw.toBlocking().exchange(HttpRequest.GET("/books").bearerAuth(PostgreSQL.bearerToken())).getStatus());
        }
    }

    /**
     * A client created outside the registry: no service id, so no filters apply.
     */
    private static HttpClient rawClient() throws MalformedURLException {
        return HttpClient.create(new URL(PostgreSQL.getUrl()));
    }
}
