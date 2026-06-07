plugins {
    id("ivy.android-library")
    id("ivy.hilt")
    id("ivy.kotlinx-serialization")
}

android {
    namespace = "com.ivy.base"
}

dependencies {
    testImplementation(libs.bundles.testing)
}
