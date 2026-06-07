plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.base"
}

dependencies {
    implementation(libs.javax.inject)

    testImplementation(libs.bundles.testing)
}
