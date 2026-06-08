plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.budgets"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
