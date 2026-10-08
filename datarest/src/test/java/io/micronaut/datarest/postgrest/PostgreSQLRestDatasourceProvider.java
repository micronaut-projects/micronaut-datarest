package io.micronaut.datarest.postgrest;

import io.micronaut.datarest.testutils.PostgrestContainers;
import io.micronaut.datarest.tck.RestDatasourceProvider;

import java.util.Map;

public class PostgreSQLRestDatasourceProvider implements RestDatasourceProvider {
    @Override
    public Map<String, String> getProperties() {
        return PostgrestContainers.getProperties();
    }
}
