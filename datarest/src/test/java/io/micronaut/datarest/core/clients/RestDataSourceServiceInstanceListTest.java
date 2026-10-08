package io.micronaut.datarest.core.clients;

import io.micronaut.context.BeanContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.discovery.ServiceInstanceList;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "restdatasources.default.url", value = "http://localhost:3000")
@Property(name = "restdatasources.prefixed.url", value = "http://proxy.example.com/postgrest/")
@Property(name = "restdatasources.ords.url", value = "http://localhost:8080/ords/hr")
@MicronautTest
class RestDataSourceServiceInstanceListTest {

    @Inject
    BeanContext beanContext;

    @Test
    void eachDataSourceIsRegisteredAsAServiceWithItsPathAsContextPath() {
        ServiceInstanceList plain = serviceInstanceList("default");
        assertEquals("restdatasource-default", plain.getID());
        assertEquals(URI.create("http://localhost:3000"), plain.getInstances().getFirst().getURI());
        assertEquals(Optional.empty(), plain.getContextPath());

        assertEquals(Optional.of("/postgrest"), serviceInstanceList("prefixed").getContextPath());
        assertEquals(Optional.of("/ords/hr"), serviceInstanceList("ords").getContextPath());
    }

    @Test
    void everyDataSourceHasAClientUnderItsServiceId() {
        for (String name : List.of("default", "prefixed", "ords")) {
            RestDataSourceClient client = beanContext.getBean(RestDataSourceClient.class, Qualifiers.byName(name));
            assertEquals("restdatasource-" + name, client.getServiceId());
        }
    }

    private ServiceInstanceList serviceInstanceList(String name) {
        return beanContext.getBean(ServiceInstanceList.class, Qualifiers.byName(name));
    }
}
