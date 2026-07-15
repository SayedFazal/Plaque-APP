plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.periocompliance.ai"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.periocompliance.ai"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // Where the app looks for the backend. Override without touching code:
    //     ./gradlew assembleDemo -Pperio.apiBaseUrl=https://your-api.onrender.com/
    // Must end in a slash — Retrofit requires it.
    val deployedApiUrl = providers.gradleProperty("perio.apiBaseUrl")
        .getOrElse("https://periocompliance-api.onrender.com/")

    buildTypes {
        debug {
            isMinifyEnabled = false
            // 127.0.0.1 is the DEVICE's own loopback, tunnelled to the laptop by
            // `adb reverse tcp:3000 tcp:3000`. Not 10.0.2.2 (that is the emulator's host alias) and
            // not a LAN IP (venue Wi-Fi usually isolates clients, so it dies exactly when you demo).
            buildConfigField("String", "API_BASE_URL", "\"http://127.0.0.1:3000/\"")
        }

        /**
         * The build you actually demo with.
         *
         * Debug-signed, so it installs like a debug build with no keystore — but it points at the
         * deployed HTTPS backend, so it needs no USB cable, no `adb reverse`, and no laptop. A
         * presentation that depends on a cable staying seated is one kicked table leg from failing.
         */
        create("demo") {
            initWith(getByName("debug"))
            isMinifyEnabled = false
            applicationIdSuffix = ".demo"
            versionNameSuffix = "-demo"
            matchingFallbacks += listOf("debug")
            buildConfigField("String", "API_BASE_URL", "\"$deployedApiUrl\"")
        }

        release {
            isMinifyEnabled = true
            isShrinkResources = true
            buildConfigField("String", "API_BASE_URL", "\"$deployedApiUrl\"")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.kotlinx.coroutines.android)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.security.crypto)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
}
