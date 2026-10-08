package io.micronaut.datarest.docs;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.context.env.PropertySource;
import io.micronaut.datarest.testutils.PostgrestTestPropertyProvider;

import java.util.Map;

/**
 * Points the {@code default} REST data source of the Python tests at the shared PostgREST container, as
 * {@link PostgrestTestPropertyProvider} does for the Java, Kotlin and Groovy suites. Micronaut Test calls a
 * {@code TestPropertyProvider} before the application context, and with it the GraalPy runtime, exists, so a
 * Python test class cannot implement it.
 */
@ContextConfigurer
public class PostgrestTestConfigurer implements ApplicationContextConfigurer {

    @Override
    public void configure(ApplicationContext applicationContext) {
        Map<String, String> properties = PostgrestTestPropertyProvider.properties();
        applicationContext.getEnvironment().addPropertySource(PropertySource.of("postgrest", Map.copyOf(properties)));
    }
}
