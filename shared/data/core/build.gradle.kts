plugins {
    id("ivy.android-library")
    id("ivy.hilt")
    id("ivy.kotlinx-serialization")
    id("ivy.room")
    id("ivy.integration.testing")
}

android {
    namespace = "com.ivy.data"
}

dependencies {
    implementation(projects.shared.data.api)
    implementation(projects.shared.data.model)

    implementation(libs.bundles.arrow)
    implementation(libs.androidx.core.ktx)
    implementation(libs.datastore)
    implementation(libs.bundles.ktor)

    testImplementation(projects.shared.testSupport)
    testImplementation(libs.bundles.testing)
}
