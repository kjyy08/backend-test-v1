tasks.jar {
    enabled = false
}

tasks.bootJar {
    enabled = true
}

dependencies {
    implementation(projects.modules.domain)
    implementation(projects.modules.application)
    implementation(projects.modules.infrastructure.persistence)
    implementation(projects.modules.infrastructure.metrics)
    implementation(projects.modules.external.pgClient)
    implementation(libs.spring.boot.starter.jpa)
    implementation(libs.spring.doc.openapi)
    implementation(libs.bundles.bootstrap)
    implementation(platform(libs.otlp.bom))
    implementation(libs.otlp.spring.boot.starter)

    testImplementation(libs.bundles.test)
    testImplementation(libs.spring.boot.starter.test) {
        exclude(module = "mockito-core")
    }
    testImplementation(libs.spring.mockk)
    testImplementation(libs.database.h2)

    runtimeOnly(libs.micrometer.registry.otlp)
    runtimeOnly(libs.micrometer.registry.prometheus)
}
