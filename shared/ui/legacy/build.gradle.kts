plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.ui.legacy"
}

dependencies {
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.navigation)
}
