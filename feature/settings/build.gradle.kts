plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.settings"
}

dependencies {
    implementation(libs.bundles.opencsv)
}
