plugins {
    id("ivy.android-library")
    id("androidx.room")
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)

    androidTestImplementation(libs.room.testing)
}

android {
    sourceSets {
        // Adds exported schema location as test app assets.
        getByName("androidTest").assets.srcDirs(files("$projectDir/schemas"))
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}
