plugins {
    `java-library`
}

dependencies {
    implementation(platform(libs.springBom))
    implementation(project(":application"))

    implementation("org.springframework:spring-context")
    implementation("org.springframework.boot:spring-boot")
    implementation(libs.springBootStarterJdbc)
    implementation(libs.springBootStarterDataJdbc)
    implementation(libs.springBootStarterFlyway)
    implementation(libs.flywayDatabasePostgresql)

    runtimeOnly(libs.postgresql)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    testImplementation(platform(libs.springBom))
    testImplementation(libs.springBootStarterDataJdbcTest)
    testImplementation(libs.springBootTestcontainers)
    testImplementation(libs.testcontainersJunit)
    testImplementation(libs.testcontainersPostgres)
    testRuntimeOnly(libs.junitPlatformLauncher)
}
