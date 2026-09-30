rootProject.name = "market-catalog-service"

pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

include(":application")
include(":infrastructure:app")
include(":infrastructure:rest-api")
include(":infrastructure:jdbc-storage-adapter")
include(":infrastructure:event-adapter")
