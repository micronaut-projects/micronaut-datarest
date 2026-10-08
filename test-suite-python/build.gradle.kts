plugins {
    `java-library`
    id("io.micronaut.build.internal.python")
}

description = "Test suite documenting Micronaut Data REST in Python"

repositories {
    mavenCentral()
}

dependencies {
    // The Python compiler (micronaut-inject-python) takes the compile classpath as its annotation processor
    // path, so the processors are test dependencies rather than annotation processor ones.
    testImplementation(mn.micronaut.inject.python.test)
    testImplementation(mn.micronaut.context.python)
    testImplementation(mnSerde.micronaut.serde.processor)
    testImplementation(projects.micronautDatarestProcessor)
    testImplementation(projects.micronautDatarest)
    testImplementation(projects.testSuiteUtils)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mnTest.micronaut.test.junit5)
    // The Java helper of src/test/java is processed by javac
    testAnnotationProcessor(mn.micronaut.inject.java)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    systemProperty("micronaut.python.pool.enabled", "false")
}
