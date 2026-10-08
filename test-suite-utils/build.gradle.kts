plugins {
    id("io.micronaut.build.internal.java-base")
    `java-library`
}

description = "PostgREST containers shared by the test suites"

repositories {
    mavenCentral()
}

dependencies {
    api(mnTest.micronaut.test.core)
    implementation(platform(mnTest.boms.testcontainers))
    implementation(libs.testcontainers.postgresql)
    runtimeOnly(mnSql.postgresql)
}
