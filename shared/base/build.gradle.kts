plugins {
    id("ivy.android-library")
    id("ivy.hilt")
    id("ivy.kotlinx-serialization")
}

android {
    namespace = "com.ivy.base"
}

dependencies {
    implementation(libs.compose.runtime)
}
