plugins {
    id("ivy.compose")
    id("ivy.hilt")
}

android {
    namespace = "com.ivy.ui"
}

dependencies {
    implementation(projects.shared.data.model)

    testImplementation(libs.bundles.testing)
}
