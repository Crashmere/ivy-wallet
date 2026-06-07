plugins {
    id("ivy.android-library")
    id("ivy.hilt")
    id("ivy.kotlinx-serialization")
}

android {
    namespace = "com.ivy.base"
}

dependencies {
    implementation(libs.bundles.arrow)
    implementation(libs.androidx.lifecycle.livedata.core)

    testImplementation(libs.bundles.testing)
}
