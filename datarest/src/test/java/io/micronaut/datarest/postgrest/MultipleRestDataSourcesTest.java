package io.micronaut.datarest.postgrest;

import io.micronaut.datarest.testutils.PostgrestContainers;
import io.micronaut.context.BeanContext;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.datarest.core.repositories.ReactiveRestGenericRepository;
import io.micronaut.datarest.core.repositories.ReactorRestGenericRepository;
import io.micronaut.datarest.core.repositories.RestGenericRepository;
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
        Map<String, String> properties = new HashMap<>(PostgrestContainers.getProperties("default"));
        properties.putAll(PostgrestContainers.getProperties("other"));
        return properties;
    }

    @Test
    void aRepositoryOfEachKindExistsPerDataSource() {
        for (String name : new String[] {"default", "other"}) {
            RestGenericRepository blocking = assertDoesNotThrow(() -> beanContext.getBean(RestGenericRepository.class, Qualifiers.byName(name)));
            ReactiveRestGenericRepository reactive = assertDoesNotThrow(() -> beanContext.getBean(ReactiveRestGenericRepository.class, Qualifiers.byName(name)));
            ReactorRestGenericRepository reactor = assertDoesNotThrow(() -> beanContext.getBean(ReactorRestGenericRepository.class, Qualifiers.byName(name)));
            assertEquals(0, blocking.count("books"));
            assertEquals(0, Mono.from(reactive.count("books")).block());
            assertEquals(0, reactor.count("books").block());
        }
        assertNotSame(
            beanContext.getBean(ReactorRestGenericRepository.class, Qualifiers.byName("default")),
            beanContext.getBean(ReactorRestGenericRepository.class, Qualifiers.byName("other")));
    }
}
