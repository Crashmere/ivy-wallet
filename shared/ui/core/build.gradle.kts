plugins {
    id("ivy.compose")
    id("ivy.hilt")
}

android {
    namespace = "com.ivy.ui"
}

dependencies {
    implementation(projects.shared.base)

    testImplementation(projects.shared.baseTesting)
    testImplementation(libs.bundles.testing)
}
