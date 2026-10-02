plugins {
    `java-library`
}

dependencies {
    api(libs.slf4jApi)

    compileOnly(libs.lombok)

    annotationProcessor(libs.lombok)

    testImplementation(platform(libs.springBom))
    testImplementation(libs.junitJupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
}