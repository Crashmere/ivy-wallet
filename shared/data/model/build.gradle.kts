plugins {
    id("ivy.android-library")
    id("ivy.kotlinx-serialization")
}

android {
    namespace = "com.ivy.data.model"
}

dependencies {
    api(libs.bundles.arrow)
    api(libs.kotlinx.collections.immutable)

    testImplementation(libs.bundles.testing)
}
