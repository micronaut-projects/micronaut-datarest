plugins {
    id("io.micronaut.build.internal.datarest-base")
    id("io.micronaut.build.internal.bom")
}

micronautBuild {
    // New module: there is no previously published release to compare against.
    binaryCompatibility {
        enabled.set(false)
    }
}
