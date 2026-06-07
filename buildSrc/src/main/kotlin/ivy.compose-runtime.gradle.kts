plugins {
    id("ivy.android-library")
    org.jetbrains.kotlin.plugin.compose
}

android {
    buildFeatures {
        compose = true
    }
}

composeCompiler {
    reportsDestination = layout.buildDirectory.dir("compose_compiler")
    metricsDestination = layout.buildDirectory.dir("compose_compiler")
}

dependencies {
    implementation(libs.compose.runtime)
    implementation(libs.compose.ui)
}
