plugins {
    id("ivy.android-library")
    id("ivy.hilt")
    id("ivy.integration.testing")
}

android {
    namespace = "com.ivy.domain"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.core)

    implementation(libs.datastore)
    implementation(libs.bundles.opencsv)

    testImplementation(projects.shared.baseTesting)
    testImplementation(projects.shared.data.modelTesting)

    androidTestImplementation(projects.shared.baseTesting)
    androidTestImplementation(libs.bundles.ktor)
    androidTestImplementation(libs.bundles.room)
    androidTestImplementation(libs.room.testing)
    androidTestImplementation(libs.mockk.android)
}
