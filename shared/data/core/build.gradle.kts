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
    implementation(projects.shared.base)
    implementation(projects.shared.data.api)
    api(projects.shared.data.model)

    implementation(libs.bundles.arrow)
    implementation(libs.datastore)
    implementation(libs.bundles.ktor)
    implementation(libs.timber)

    testImplementation(projects.shared.baseTesting)
    testImplementation(projects.shared.data.modelTesting)
    testImplementation(libs.bundles.testing)
    androidTestImplementation(projects.shared.baseTesting)
}
