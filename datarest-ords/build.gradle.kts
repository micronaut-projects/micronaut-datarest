plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    api(projects.micronautDatarest)
    implementation(mn.micronaut.json.core)
    testImplementation(projects.micronautDatarestTck)
    testImplementation(mn.micronaut.http.client)
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.oracle.free)
    testRuntimeOnly(mnSql.ojdbc17)
}
