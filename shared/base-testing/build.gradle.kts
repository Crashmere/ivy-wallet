plugins {
    id("ivy.android-library")
}

android {
    namespace = "com.ivy.base.testing"
}

dependencies {
    api(projects.shared.base)
}
