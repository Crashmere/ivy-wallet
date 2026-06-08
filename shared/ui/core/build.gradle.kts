plugins {
    id("ivy.compose")
    id("ivy.hilt")
}

android {
    namespace = "com.ivy.ui"
}

dependencies {
    implementation(projects.shared.data.model)

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)

    testImplementation(libs.bundles.testing)
}
