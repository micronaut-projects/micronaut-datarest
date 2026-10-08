plugins {
    id("io.micronaut.build.internal.datarest-module")
}

dependencies {
    compileOnly(mn.micronaut.core.processor)
    implementation(projects.micronautDatarest)
    implementation(mnSourcegen.micronaut.sourcegen.generator)
    implementation(mnSourcegen.micronaut.sourcegen.generator.java)
    testImplementation(mn.micronaut.core.processor)
    testImplementation(mn.micronaut.inject.java.test)
}
