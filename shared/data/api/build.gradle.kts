plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.data.api"
}

dependencies {
    api(projects.shared.data.model)
}
