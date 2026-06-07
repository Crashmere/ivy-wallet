plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.data.model"
}

dependencies {
    implementation(projects.shared.base)

    api(libs.bundles.arrow)
    api(libs.kotlinx.collections.immutable)
}
