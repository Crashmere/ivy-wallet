plugins {
    id("ivy.feature")
}

android {
    namespace = "com.ivy.feature.wallet"
}

dependencies {
    implementation(project(":feature:analytics"))
    implementation(project(":feature:settings"))
}
