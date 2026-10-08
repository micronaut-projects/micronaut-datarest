plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    api(projects.micronautDatarest)
    testImplementation(mn.micronaut.http.client)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.postgresql)
    testAnnotationProcessor(mnSerde.micronaut.serde.processor)
    testImplementation(mnSerde.micronaut.serde.jackson)
    testRuntimeOnly(mnSql.postgresql)
}
