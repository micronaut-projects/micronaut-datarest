plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    annotationProcessor(mn.micronaut.inject.java)
    annotationProcessor(mnSerde.micronaut.serde.processor)
    annotationProcessor(projects.micronautDatarestProcessor)
    api(projects.micronautDatarest)
    api(mnTest.micronaut.test.junit5)
    api(mnReactor.micronaut.reactor)
    implementation(mnSerde.micronaut.serde.jackson)
}
