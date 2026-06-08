plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.exchangerates"
}

dependencies {
    implementation(projects.shared.ui.legacy)
}
