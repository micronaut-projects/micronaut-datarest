package io.micronaut.datarest.core.conf;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "restdatasources.default.url", value = "http://localhost:8081")
@MicronautTest
class RestDataSourceConfigurationTest {

    @Inject
    BeanContext beanContext;

    @Test
    void testRestDataSourceConfiguration() {
        assertTrue(beanContext.containsBean(RestDataSourceConfiguration.class, Qualifiers.byName("default")));
    }
}
