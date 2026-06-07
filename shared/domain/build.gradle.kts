plugins {
    id("ivy.kotlin-library")
}

dependencies {
    implementation(projects.shared.base)
    api(projects.shared.data.api)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.opencsv)
    implementation(libs.javax.inject)

    testImplementation(projects.shared.baseTesting)
    testImplementation(projects.shared.data.modelTesting)
    testImplementation(libs.bundles.testing)
}
