plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.transaction"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
