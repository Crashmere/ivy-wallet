plugins {
    id("ivy.compose")
}

android {
    namespace = "com.ivy.ui.legacy"
}

dependencies {
    implementation(projects.shared.ui.core)

    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.activity)
    implementation(libs.keval)
    implementation(libs.kotlinx.collections.immutable)
}
