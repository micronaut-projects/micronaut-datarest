plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    api(mn.micronaut.http.client.core)
    api(mnData.micronaut.data.model)
    compileOnly(mnReactor.micronaut.reactor)
    testImplementation(mn.micronaut.http.client)
    testImplementation(projects.micronautDatarestTck)
    testImplementation(projects.testSuiteUtils)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.oracle.free)
    testRuntimeOnly(mnSql.ojdbc17)
    testRuntimeOnly(mnSerde.micronaut.serde.jackson)
}
