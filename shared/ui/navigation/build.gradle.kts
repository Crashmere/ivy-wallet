plugins {
    id("ivy.compose")
}

android {
    namespace = "com.ivy.navigation"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.ui.core)
    implementation(libs.javax.inject)
}
