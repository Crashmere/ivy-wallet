plugins {
    id("ivy.android-library")
    id("ivy.compose-runtime")
    id("ivy.integration.testing")
    id("ivy.room")
}

android {
    namespace = "com.ivy.domain"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.core)

    implementation(libs.datastore)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.opencsv)

    testImplementation(projects.shared.baseTesting)
    testImplementation(projects.shared.data.modelTesting)

    androidTestImplementation(projects.shared.baseTesting)
    androidTestImplementation(libs.mockk.android)
}
