plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.importdata"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.model)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.legacy)
    implementation(projects.shared.ui.navigation)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.opencsv)
}
