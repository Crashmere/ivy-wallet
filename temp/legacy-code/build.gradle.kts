plugins {
    id("ivy.android-library")
    id("ivy.compose-runtime")
    id("ivy.hilt")
}

android {
    namespace = "com.ivy.legacy"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.model)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.legacy)

    implementation(libs.compose.foundation)
}
