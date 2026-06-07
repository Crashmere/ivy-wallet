plugins {
    id("ivy.kotlin-library")
}

dependencies {
    implementation(projects.shared.data.model)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.testing)

    testImplementation(libs.bundles.testing)
}
