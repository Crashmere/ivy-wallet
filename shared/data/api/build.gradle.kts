plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.data.api"
}

dependencies {
    api(projects.shared.base)
    api(projects.shared.data.model)

    api(libs.kotlin.coroutines.core)
}
