plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.categories"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
