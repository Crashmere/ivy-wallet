plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.main"
}

dependencies {
    implementation(projects.feature.accounts)
    implementation(projects.feature.home)
    implementation(projects.shared.data.model)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.legacy)
    implementation(projects.shared.ui.navigation)

    implementation(libs.compose.viewmodel)
}
