plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.transactions"
}

dependencies {
    implementation(projects.shared.ui.legacy)

    implementation(libs.compose.activity)
}
