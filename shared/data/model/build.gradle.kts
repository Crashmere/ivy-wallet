plugins {
    id("ivy.kotlin-library")
    id("ivy.kotlinx-serialization")
}

dependencies {
    api(libs.bundles.arrow)
    api(libs.kotlinx.collections.immutable)

    testImplementation(libs.bundles.testing)
}
