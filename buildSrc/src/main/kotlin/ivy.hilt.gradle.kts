import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

plugins {
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

pluginManager.withPlugin("org.jetbrains.kotlin.android") {
    extensions.configure<KotlinAndroidProjectExtension>("kotlin") {
        sourceSets.all {
            kotlin.srcDir("build/generated/ksp/$name/kotlin")
        }
    }
}

dependencies {
    add("implementation", libs.bundles.hilt)
    add("ksp", catalog.library("hilt-compiler"))
}
