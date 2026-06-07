import com.android.build.gradle.LibraryExtension

pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension>("android") {
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
}

dependencies {
    add("androidTestImplementation", libs.bundles.integration.testing)
}
