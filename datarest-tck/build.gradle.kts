plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    annotationProcessor(mnSerde.micronaut.serde.processor)
    api(projects.micronautDatarest)
    api(mnTest.micronaut.test.junit5)
    api(mnReactor.micronaut.reactor)
    implementation(mnSerde.micronaut.serde.jackson)
}
