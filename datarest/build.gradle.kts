plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    api(mn.micronaut.http.client.core)
    api(mnData.micronaut.data.model)
    api(mnReactor.micronaut.reactor)
    testImplementation(mn.micronaut.http.client)
    testRuntimeOnly(mnSerde.micronaut.serde.jackson)
}
