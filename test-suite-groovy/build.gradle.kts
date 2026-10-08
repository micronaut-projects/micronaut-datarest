plugins {
    groovy
    `java-library`
}

description = "Test suite documenting Micronaut Data REST in Groovy"

repositories {
    mavenCentral()
}

dependencies {
    testCompileOnly(mn.micronaut.inject.groovy)
    testCompileOnly(mnSerde.micronaut.serde.processor)
    // The Groovy compiler runs the visitor, so SourceGen must be on its compile classpath
    testCompileOnly(projects.micronautDatarestProcessor)
    testCompileOnly(mnSourcegen.micronaut.sourcegen.generator)
    testCompileOnly(mnSourcegen.micronaut.sourcegen.generator.java)
    testCompileOnly(mnSourcegen.micronaut.sourcegen.annotations)
    testImplementation(projects.micronautDatarest)
    testImplementation(projects.testSuiteUtils)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mnTest.micronaut.test.spock)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
