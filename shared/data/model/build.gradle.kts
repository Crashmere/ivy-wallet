plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.data.model"
}

dependencies {
    implementation(projects.shared.base)
    api(libs.kotlinx.collections.immutable)
}
