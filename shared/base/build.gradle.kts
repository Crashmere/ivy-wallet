plugins {
    id("ivy.android-library")
    id("ivy.hilt")
}

android {
    namespace = "com.ivy.base"
}

dependencies {
    testImplementation(libs.bundles.testing)
}
