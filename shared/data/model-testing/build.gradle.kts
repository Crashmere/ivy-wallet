plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.data.model.testing"
}

dependencies {
    implementation(projects.shared.data.model)

    implementation(libs.bundles.arrow)
    implementation(libs.bundles.testing)

    testImplementation(libs.bundles.testing)
}
