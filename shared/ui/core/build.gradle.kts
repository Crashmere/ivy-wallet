plugins {
    id("ivy.compose")
    id("ivy.hilt")
}

android {
    namespace = "com.ivy.ui"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.data.model)

    testImplementation(projects.shared.baseTesting)
    testImplementation(libs.bundles.testing)
}
