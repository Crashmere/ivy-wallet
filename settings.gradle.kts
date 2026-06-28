enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "IvyWallet"
include(":app")
include(":feature:analytics")
include(":feature:settings")
include(":feature:wallet")
include(":shared:data:api")
include(":shared:data:core")
include(":shared:data:model")
include(":shared:test-support")
include(":shared:domain")
include(":shared:ui:core")
include(":shared:ui:navigation")
