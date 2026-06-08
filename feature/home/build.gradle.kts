plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.home"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
