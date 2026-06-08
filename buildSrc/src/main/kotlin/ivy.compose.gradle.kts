plugins {
    org.jetbrains.kotlin.plugin.compose
    id("ivy.android-library")
}

android {
    // Compose
    buildFeatures {
        compose = true
    }

    lint {
        disable += "MissingTranslation"
        abortOnError = false
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    implementation(libs.bundles.compose)
}
