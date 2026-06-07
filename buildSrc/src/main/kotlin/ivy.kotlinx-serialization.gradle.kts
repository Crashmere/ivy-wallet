plugins {
    id("org.jetbrains.kotlin.plugin.serialization")
}

dependencies {
    add("implementation", catalog.library("kotlinx-serialization-json"))
}
