plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.piechart"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
