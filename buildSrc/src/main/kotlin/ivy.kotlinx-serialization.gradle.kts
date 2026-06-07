plugins {
    id("ivy.android-library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    implementation(catalog.library("kotlinx-serialization-json"))
}
