plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.reports"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
