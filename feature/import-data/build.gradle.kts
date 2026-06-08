plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.importdata"
}

dependencies {
    implementation(libs.compose.activity)
    implementation(libs.bundles.opencsv)
}
