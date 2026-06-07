import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
}

val javaVersion = catalog.version("jvm-target")

java {
    sourceCompatibility = JavaVersion.valueOf("VERSION_$javaVersion")
    targetCompatibility = JavaVersion.valueOf("VERSION_$javaVersion")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion))
    }
}

gradle.projectsEvaluated {
    tasks.withType<Test> {
        maxHeapSize = "2048m"
    }
}

dependencies {
    implementation(libs.bundles.kotlin)
}
