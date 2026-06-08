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
    implementation(libs.compose.activity)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.recyclerview)
    implementation(libs.keval)

    testImplementation(libs.bundles.testing)
}
