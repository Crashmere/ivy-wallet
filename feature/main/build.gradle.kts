plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.main"
}

dependencies {
    implementation(projects.feature.accounts)
    implementation(projects.feature.home)

    implementation(libs.compose.activity)
}
