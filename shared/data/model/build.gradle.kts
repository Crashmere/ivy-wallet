plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.data.model"
}

dependencies {
    implementation(libs.compose.runtime)
}
