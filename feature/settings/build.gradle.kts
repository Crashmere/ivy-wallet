plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.settings"
}

dependencies {
    implementation(projects.shared.ui.legacy)

    implementation(libs.compose.activity)
}
