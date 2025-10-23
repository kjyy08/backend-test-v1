tasks.jar {
    enabled = true
}

tasks.bootJar {
    enabled = false
}

dependencies {
    implementation(projects.modules.application)
    implementation(libs.micrometer.registry.otlp)
    implementation(libs.spring.context)
}
