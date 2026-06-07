plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.ui.legacy"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.model)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.navigation)

    implementation(libs.androidx.recyclerview)
    implementation(libs.keval)
}
