package io.micronaut.datarest.core.clients;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.exceptions.BeanInstantiationException;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A data source entry whose {@code url} key is mistyped must fail with a message naming the property.
 */
@Property(name = "restdatasources.broken.ur", value = "http://localhost:3000")
@MicronautTest
class RestDataSourceMissingUrlTest {

    @Inject
    BeanContext beanContext;

    @Test
    void missingUrlIsReportedAsAConfigurationError() {
        BeanInstantiationException e = assertThrows(BeanInstantiationException.class,
            () -> beanContext.getBean(RestDataSourceClient.class, Qualifiers.byName("broken")));
        Throwable cause = rootCause(e);
        assertInstanceOf(ConfigurationException.class, cause);
        assertTrue(cause.getMessage().contains("restdatasources.broken.url"), cause.getMessage());
    }

    private static Throwable rootCause(Throwable t) {
        Throwable cause = t;
        while (cause.getCause() != null && cause.getCause() != cause) {
            cause = cause.getCause();
        }
        return cause;
    }
}
