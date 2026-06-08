plugins {
    id("ivy.compose")
}

android {
    namespace = "com.ivy.ui.navigation"
}

dependencies {
    implementation(projects.shared.ui.core)
    implementation(libs.compose.viewmodel)
    implementation(libs.kotlinx.collections.immutable)
}
