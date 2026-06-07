plugins {
    id("ivy.compose")
}

android {
    namespace = "com.ivy.ui"
}

dependencies {
    implementation(projects.shared.base)
    implementation(projects.shared.domain)

    implementation(libs.datastore)

    testImplementation(projects.shared.baseTesting)
}
