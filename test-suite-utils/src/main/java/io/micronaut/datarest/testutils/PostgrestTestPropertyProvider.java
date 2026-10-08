package io.micronaut.datarest.testutils;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.test.support.TestPropertyProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Points the {@code default} REST data source of a test at the shared PostgREST container and exposes the JWT the
 * container expects as the {@code books.token} property, which the documented bearer token filter reads.
 */
public interface PostgrestTestPropertyProvider extends TestPropertyProvider {

    /**
     * Property holding the JWT PostgREST expects.
     */
    String TOKEN_PROPERTY = "books.token";

    /**
     * @return the REST data source properties plus the token property
     */
    static Map<String, String> properties() {
        Map<String, String> properties = new HashMap<>(PostgrestContainers.getProperties());
        properties.put(TOKEN_PROPERTY, PostgrestContainers.bearerToken());
        return properties;
    }

    @Override
    @NonNull
    default Map<String, String> getProperties() {
        return properties();
    }
}
