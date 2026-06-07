plugins {
    id("ivy.kotlin-library")
}

dependencies {
    api(projects.shared.data.model)

    api(libs.bundles.arrow)
    api(libs.kotlin.coroutines.core)
}
