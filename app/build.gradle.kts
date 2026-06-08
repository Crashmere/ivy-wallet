import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    org.jetbrains.kotlin.plugin.compose
    id("dagger.hilt.android.plugin")
    id("com.google.devtools.ksp")
}

val javaVersion = libs.versions.jvm.target.get()

android {
    namespace = "com.ivy.wallet"
    compileSdk = libs.versions.compile.sdk.get().toInt()

    defaultConfig {
        applicationId = "com.ivy.wallet"
        minSdk = libs.versions.min.sdk.get().toInt()
        targetSdk = libs.versions.compile.sdk.get().toInt()
        versionName = libs.versions.version.name.get()
        versionCode = libs.versions.version.code.get().toInt()
    }

    androidResources {
        generateLocaleConfig = true
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("../debug.jks")
            storePassword = "IVY7834!DEbug"
            keyAlias = "debug"
            keyPassword = "IVY7834!DEbug"
        }

    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            isDebuggable = false
            isDefault = false

            signingConfig = signingConfigs.getByName("debug")

            resValue("string", "app_name", "Ivy Wallet")
        }

        debug {
            isMinifyEnabled = false
            isShrinkResources = false

            isDebuggable = true
            isDefault = true

            signingConfig = signingConfigs.getByName("debug")

            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Ivy Wallet Debug")
        }

        create("demo") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            matchingFallbacks.add("release")

            isDebuggable = false
            isDefault = false

            signingConfig = signingConfigs.getByName("debug")

            applicationIdSuffix = ".debug"
            resValue("string", "app_name", "Ivy Wallet")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.valueOf("VERSION_$javaVersion")
        targetCompatibility = JavaVersion.valueOf("VERSION_$javaVersion")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        abortOnError = false
        checkReleaseBuilds = false
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.fromTarget(javaVersion))
    }
}

dependencies {
    implementation(projects.feature.balance)
    implementation(projects.feature.budgets)
    implementation(projects.feature.categories)
    implementation(projects.feature.editTransaction)
    implementation(projects.feature.exchangeRates)
    implementation(projects.feature.home)
    implementation(projects.feature.importData)
    implementation(projects.feature.loans)
    implementation(projects.feature.main)
    implementation(projects.feature.piechart)
    implementation(projects.feature.plannedPayments)
    implementation(projects.feature.reports)
    implementation(projects.feature.search)
    implementation(projects.feature.settings)
    implementation(projects.feature.transactions)
    implementation(projects.shared.data.core)
    implementation(projects.shared.data.model)
    implementation(projects.shared.domain)
    implementation(projects.shared.ui.core)
    implementation(projects.shared.ui.legacy)
    implementation(projects.shared.ui.navigation)

    implementation(libs.bundles.kotlin)
    implementation(libs.bundles.kotlin.android.runtime)
    implementation(libs.bundles.compose)
    implementation(libs.bundles.activity)
    implementation(libs.compose.activity)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.biometrics)

    implementation(libs.bundles.hilt)
    implementation(libs.hilt.work)
    implementation(libs.material)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.work)
}
