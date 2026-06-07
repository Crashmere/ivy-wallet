plugins {
    id("ivy.kotlin-library")
}

dependencies {
    api(projects.shared.data.api)

    implementation(libs.bundles.arrow)
    implementation(libs.javax.inject)

    testImplementation(projects.shared.data.modelTesting)
    testImplementation(libs.bundles.opencsv)
    testImplementation(libs.bundles.testing)
}
