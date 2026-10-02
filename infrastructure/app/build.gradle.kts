plugins {
    alias(libs.plugins.springBoot)
    alias(libs.plugins.springDependencyManagement)
    java
}

dependencies {
    implementation(project(":application"))
    implementation(project(":infrastructure:rest-api"))
    implementation(project(":infrastructure:jdbc-storage-adapter"))
    implementation(libs.springBootStarterWebmvc)
    implementation(libs.springBootStarterActuator)
    implementation(libs.springBootStarterValidation)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)
    annotationProcessor(libs.mapstructProcessor)

    testImplementation(libs.springBootStarterWebmvcTest)
    testImplementation(libs.springBootRestclient) // TestRestTemplate needs RestTemplateBuilder
    testImplementation(libs.springBootTestcontainers)
    testImplementation(libs.testcontainersJunit)
    testImplementation(libs.testcontainersPostgres)

    testRuntimeOnly(libs.junitPlatformLauncher)
}
