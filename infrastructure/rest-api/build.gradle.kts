import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    `java-library`
    alias(libs.plugins.openapiGenerator)
}

dependencies {
    implementation(platform(libs.springBom))
    implementation(project(":application"))
    implementation(libs.springWeb)
    implementation("org.springframework:spring-webmvc")
    implementation(libs.springContext)
    implementation(libs.jakartaValidationApi)
    implementation(libs.jacksonDatabind)
    implementation(libs.openapiJacksonNullable)
    implementation(libs.swaggerAnnotations)
    implementation(libs.mapstruct)

    compileOnly(libs.lombok)
    compileOnly(libs.jakartaAnnotationApi)
    compileOnly(libs.jakartaServletApi)

    annotationProcessor(libs.mapstructProcessor)
    annotationProcessor(libs.lombok)

    testImplementation(libs.junitJupiter)
    testRuntimeOnly(libs.junitPlatformLauncher)
}

val openApiGeneratedDir = layout.buildDirectory.dir("generated/openapi")

val openApiContractDir = project(":infrastructure:rest-api:market-catalog-service-open-api")
    .projectDir
    .resolve("src/main/resources/openapi")

tasks.named<GenerateTask>("openApiGenerate") {
    generatorName.set("spring")
    cleanupOutput.set(true) // wipe stale generated files so removed contract schemas don't linger
    // Track every schema file, not just the root, so edits to $ref'd schemas re-trigger generation.
    inputs.dir(openApiContractDir)
    inputSpec.set(openApiContractDir.resolve("openapi.yaml").absolutePath)
    outputDir.set(openApiGeneratedDir.get().asFile.absolutePath)
    apiPackage.set("com.trading.catalog.restapi.generated.api")
    modelPackage.set("com.trading.catalog.restapi.generated.model")
    modelNameSuffix.set("WebDto")
    configOptions.set(
        mapOf(
            "interfaceOnly" to "true",
            "useSpringBoot4" to "true",
            "useJackson3" to "true",
            "generateJsonIncludeAnnotations" to "false",
            "generateJsonSetterNullsAnnotations" to "false",
            "dateLibrary" to "java8",
            "useTags" to "true",
            "skipDefaultInterface" to "true",
            "useResponseEntity" to "false"
        )
    )
}

sourceSets {
    main {
        java {
            srcDir(openApiGeneratedDir.map { it.dir("src/main/java") })
        }
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}
