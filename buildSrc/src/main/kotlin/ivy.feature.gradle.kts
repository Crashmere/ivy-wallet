plugins {
    id("ivy.compose")
    id("ivy.hilt")
}

dependencies {
    implementation(project(":shared:data:model"))
    implementation(project(":shared:domain"))
    implementation(project(":shared:ui:core"))
    implementation(project(":shared:ui:navigation"))
}
