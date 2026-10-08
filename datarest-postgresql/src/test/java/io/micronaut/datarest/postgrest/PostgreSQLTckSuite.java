package io.micronaut.datarest.postgrest;

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
}
