plugins {
    id("ivy.android-library")
    id("ivy.compose-runtime")
    id("ivy.room")
    id("ivy.integration.testing")
}

android {
    namespace = "com.ivy.data"
}

dependencies {
    implementation(projects.shared.base)
    api(projects.shared.data.model)

    api(libs.datastore)
    implementation(libs.bundles.ktor)

    testImplementation(projects.shared.baseTesting)
    testImplementation(projects.shared.data.modelTesting)
    androidTestImplementation(projects.shared.baseTesting)
}
