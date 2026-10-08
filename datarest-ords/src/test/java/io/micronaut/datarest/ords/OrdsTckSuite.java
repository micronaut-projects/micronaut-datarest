package io.micronaut.datarest.ords;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Runs the Micronaut Data REST TCK against ORDS.
 */
@Suite
@SuiteDisplayName("Micronaut Data REST TCK - ORDS")
@SelectPackages("io.micronaut.datarest.tck")
public class OrdsTckSuite {
}
