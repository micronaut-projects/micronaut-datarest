plugins {
    id("io.micronaut.build.internal.datarest-module")
}
dependencies {
    testImplementation(platform(mnTest.boms.testcontainers))
    testImplementation(libs.testcontainers.oracle.free)
    testImplementation(projects.micronautDatarestTck)

}
