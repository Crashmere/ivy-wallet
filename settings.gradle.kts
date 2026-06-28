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
include(":feature:accounts")
include(":feature:analytics")
include(":feature:budgets")
include(":feature:categories")
include(":feature:edit-transaction")
include(":feature:exchange-rates")
include(":feature:home")
include(":feature:import-data")
include(":feature:loans")
include(":feature:main")
include(":feature:planned-payments")
include(":feature:search")
include(":feature:settings")
include(":feature:transactions")
include(":shared:data:api")
include(":shared:data:core")
include(":shared:data:model")
include(":shared:test-support")
include(":shared:domain")
include(":shared:ui:core")
include(":shared:ui:navigation")
