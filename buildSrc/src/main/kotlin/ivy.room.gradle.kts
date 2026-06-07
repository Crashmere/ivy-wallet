import androidx.room.gradle.RoomExtension
import com.android.build.gradle.LibraryExtension

plugins {
    id("androidx.room")
    id("com.google.devtools.ksp")
}

dependencies {
    add("implementation", libs.bundles.room)
    add("ksp", libs.room.compiler)

    add("androidTestImplementation", libs.room.testing)
}

pluginManager.withPlugin("com.android.library") {
    extensions.configure<LibraryExtension>("android") {
        sourceSets {
            // Adds exported schema location as test app assets.
            getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
        }
    }
}

extensions.configure<RoomExtension>("room") {
    schemaDirectory("$projectDir/schemas")
}
