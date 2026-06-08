plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.importdata"
}

dependencies {
    implementation(projects.shared.ui.legacy)

    implementation(libs.compose.activity)
    implementation(libs.bundles.opencsv)
}
