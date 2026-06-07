plugins {
    id("ivy.android-library")
    id("ivy.hilt")
}

android {
    namespace = "com.ivy.domain"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.api)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.opencsv)

    testImplementation(projects.shared.baseTesting)
    testImplementation(projects.shared.data.modelTesting)
    testImplementation(libs.bundles.testing)
}
