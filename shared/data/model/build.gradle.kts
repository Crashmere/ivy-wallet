plugins {
    id("ivy.android-library")
    id("ivy.kotlinx-serialization")
}

android {
    namespace = "com.ivy.data.model"
}

dependencies {
    implementation(projects.shared.base)

    api(libs.bundles.arrow)
    api(libs.kotlinx.collections.immutable)

    testImplementation(libs.bundles.testing)
}
