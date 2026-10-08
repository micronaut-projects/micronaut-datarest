plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    api(projects.micronautDatarest)
    testImplementation(projects.micronautDatarestTck)
    testImplementation(mn.micronaut.http.client)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.postgresql)
    testRuntimeOnly(mnSql.postgresql)
}
