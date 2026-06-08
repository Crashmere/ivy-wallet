plugins {
    id("ivy.compose")
}

android {
    namespace = "com.ivy.ui.navigation"
}

dependencies {
    implementation(libs.compose.viewmodel)
    implementation(libs.kotlinx.collections.immutable)
}
