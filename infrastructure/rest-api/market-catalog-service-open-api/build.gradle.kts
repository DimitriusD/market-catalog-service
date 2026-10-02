import org.gradle.language.jvm.tasks.ProcessResources
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.openapiGenerator)
}

group = "com.trading.contracts"
version = rootProject.version.toString()

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }

    withSourcesJar()
}

val contractDir = layout.projectDirectory.dir("src/main/resources/openapi")
val bundledDir = layout.buildDirectory.dir("bundled-openapi")

tasks.named<GenerateTask>("openApiGenerate") {
    generatorName.set("openapi-yaml")
    inputSpec.set(contractDir.file("openapi.yaml").asFile.absolutePath)
    outputDir.set(bundledDir.get().asFile.absolutePath)
    inputs.dir(contractDir)
    outputs.dir(bundledDir)
}

tasks.named<ProcessResources>("processResources") {
    exclude("openapi/**")
}

tasks.named<Jar>("jar") {
    dependsOn("openApiGenerate")
    from(bundledDir) {
        include("openapi/openapi.yaml")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            groupId = "com.trading.contracts"
            artifactId = "market-catalog-service-openapi"
            version = project.version.toString()

            pom {
                name.set("Market Catalog Service OpenAPI Contract")
                description.set("OpenAPI YAML contract for market-catalog-service")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/dimitriusd/trading-contracts")

            credentials {
                username = findProperty("gpr.user") as String?
                    ?: System.getenv("GITHUB_ACTOR")
                    ?: System.getenv("GITHUB_USERNAME")

                password = findProperty("gpr.key") as String?
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
