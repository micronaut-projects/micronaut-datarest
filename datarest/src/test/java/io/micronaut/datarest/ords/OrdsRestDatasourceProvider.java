package io.micronaut.datarest.ords;

import io.micronaut.datarest.tck.RestDatasourceProvider;

import java.util.Map;

public class OrdsRestDatasourceProvider implements RestDatasourceProvider {
    @Override
    public Map<String, String> getProperties() {
        return Ords.getProperties();
    }
}
