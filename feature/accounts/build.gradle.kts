plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.accounts"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
