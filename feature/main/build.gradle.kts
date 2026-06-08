plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.main"
}

dependencies {
    implementation(projects.feature.accounts)
    implementation(projects.feature.home)
    implementation(projects.shared.ui.legacy)

    implementation(libs.compose.activity)
}
