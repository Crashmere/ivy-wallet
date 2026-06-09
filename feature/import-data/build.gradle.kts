plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.importdata"
}

dependencies {
    implementation(libs.bundles.opencsv)
}
