package io.micronaut.datarest.postgresql;

import io.micronaut.datarest.tck.RestDatasourceProvider;

import java.util.Map;

public class PostgreSQLRestDatasourceProvider implements RestDatasourceProvider {
    @Override
    public Map<String, String> getProperties() {
        return PostgreSQL.getProperties();
    }
}
