plugins {
    id("io.micronaut.build.internal.kotlin-ksp")
    `java-library`
}

description = "Test suite documenting Micronaut Data REST in Kotlin"

repositories {
    mavenCentral()
}

dependencies {
    kspTest(mn.micronaut.inject.kotlin)
    kspTest(mnSerde.micronaut.serde.processor)
    kspTest(projects.micronautDatarestProcessor)
    kspTest(mnSourcegen.micronaut.sourcegen.generator.kotlin)
    testImplementation(projects.micronautDatarest)
    testImplementation(projects.testSuiteUtils)
    testImplementation(mn.micronaut.http.client)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testImplementation(mnTest.micronaut.test.junit5)
    testRuntimeOnly(mnLogging.logback.classic)
    testRuntimeOnly(mnTest.junit.jupiter.engine)
    testRuntimeOnly(mnTest.junit.platform.launcher)
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
