plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.balance"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
