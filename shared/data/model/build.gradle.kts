plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.data.model"
}

dependencies {
    implementation(projects.shared.base)
    implementation(libs.compose.runtime)
}
