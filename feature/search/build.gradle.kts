plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.search"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
