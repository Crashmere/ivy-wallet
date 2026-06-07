plugins {
    id("ivy.android-library")
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

kotlin {
    sourceSets.all {
        kotlin.srcDir("build/generated/ksp/$name/kotlin")
    }
}

dependencies {
    implementation(libs.bundles.hilt)
    ksp(catalog.library("hilt-compiler"))
}
