plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.planned"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
