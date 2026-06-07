plugins {
    `java-library`
    id("org.jetbrains.kotlin.jvm")
}

java {
    val javaVersion = catalog.version("jvm-target")
    sourceCompatibility = JavaVersion.valueOf("VERSION_$javaVersion")
    targetCompatibility = JavaVersion.valueOf("VERSION_$javaVersion")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = catalog.version("jvm-target")
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
