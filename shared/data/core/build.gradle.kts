plugins {
    id("ivy.android-library")
    id("ivy.hilt")
    id("ivy.kotlinx-serialization")
    id("ivy.room")
}

android {
    namespace = "com.ivy.data"

    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    packaging {
        resources.pickFirsts.apply {
            add("win32-x86-64/attach_hotspot_windows.dll")
            add("win32-x86/attach_hotspot_windows.dll")
            add("META-INF/**")
            add("xsd/catalog.xml")
        }
    }
}

dependencies {
    implementation(projects.shared.data.api)
    implementation(projects.shared.data.model)

    implementation(libs.bundles.arrow)
    implementation(libs.androidx.core.ktx)
    implementation(libs.datastore)
    implementation(libs.bundles.ktor)

    testImplementation(projects.shared.testSupport)
    testImplementation(libs.bundles.testing)

    androidTestImplementation(libs.bundles.integration.testing)
}
