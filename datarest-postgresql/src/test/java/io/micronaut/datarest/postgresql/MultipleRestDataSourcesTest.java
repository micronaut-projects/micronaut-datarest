package io.micronaut.datarest.postgresql;

import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.datarest.core.repositories.ReactiveRestCrudRepository;
import io.micronaut.datarest.core.repositories.ReactorRestCrudRepository;
import io.micronaut.datarest.core.repositories.RestCrudRepository;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import io.micronaut.test.support.TestPropertyProvider;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@MicronautTest
class MultipleRestDataSourcesTest implements TestPropertyProvider {

    @Inject
    BeanContext beanContext;

    @Override
    public @NonNull Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>(PostgreSQL.getProperties("default"));
        properties.putAll(PostgreSQL.getProperties("other"));
        return properties;
    }

    @Test
    void aRepositoryOfEachKindExistsPerDataSource() {
        for (String name : new String[] {"default", "other"}) {
            RestCrudRepository blocking = assertDoesNotThrow(() -> beanContext.getBean(RestCrudRepository.class, Qualifiers.byName(name)));
            ReactiveRestCrudRepository reactive = assertDoesNotThrow(() -> beanContext.getBean(ReactiveRestCrudRepository.class, Qualifiers.byName(name)));
            ReactorRestCrudRepository reactor = assertDoesNotThrow(() -> beanContext.getBean(ReactorRestCrudRepository.class, Qualifiers.byName(name)));
            assertEquals(0, blocking.count("books"));
            assertEquals(0, Mono.from(reactive.count("books")).block());
            assertEquals(0, reactor.count("books").block());
        }
        assertNotSame(
            beanContext.getBean(ReactorRestCrudRepository.class, Qualifiers.byName("default")),
            beanContext.getBean(ReactorRestCrudRepository.class, Qualifiers.byName("other")));
    }
}
