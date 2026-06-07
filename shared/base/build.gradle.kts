plugins {
    id("ivy.kotlin-library")
}

dependencies {
    implementation(libs.javax.inject)

    testImplementation(libs.bundles.testing)
}
