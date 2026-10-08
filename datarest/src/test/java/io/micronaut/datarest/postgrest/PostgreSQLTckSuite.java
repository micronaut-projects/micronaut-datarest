package io.micronaut.datarest.postgrest;

import io.micronaut.datarest.tck.RestDatasourceProvider;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Runs the Micronaut Data REST TCK against PostgREST.
 */
@Suite
@SuiteDisplayName("Micronaut Data REST TCK - PostgreSQL")
@SelectPackages("io.micronaut.datarest.tck")
public class PostgreSQLTckSuite {

    @BeforeSuite
    static void selectPostgrest() {
        RestDatasourceProvider.select(new PostgreSQLRestDatasourceProvider());
    }

    @AfterSuite
    static void reset() {
        RestDatasourceProvider.select(null);
    }
}
