plugins {
    id("ivy.kotlin-library")
}

dependencies {
    implementation(projects.shared.data.api)

    implementation(libs.bundles.arrow)
    implementation(libs.javax.inject)

    testImplementation(projects.shared.testSupport)
    testImplementation(libs.bundles.opencsv)
    testImplementation(libs.bundles.testing)
}
