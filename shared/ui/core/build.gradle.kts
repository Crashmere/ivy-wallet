plugins {
    id("ivy.compose")
}

android {
    namespace = "com.ivy.ui"
}

dependencies {
    implementation(projects.shared.data.model)

    implementation(libs.androidx.lifecycle.viewmodel)

    testImplementation(libs.bundles.testing)
}
