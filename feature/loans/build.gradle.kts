plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.loans"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
