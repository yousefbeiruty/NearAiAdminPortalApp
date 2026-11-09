plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.screenshot)
}

android {
    namespace = "com.nearai.admin"
    compileSdk {
        version = release(37)
    }
    // Enable screenshotTest source set for Compose Preview Screenshot Testing
    experimentalProperties["android.experimental.enableScreenshotTest"] = true

    defaultConfig {
        applicationId = "com.nearai.admin"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "0.0.0"

        testInstrumentationRunner = "com.nearai.admin.CustomTestRunner"
        testInstrumentationRunnerArguments["androidx.compose.ui.test.failure.isScreenshotCaptureEnabled"] = "true"

        buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8081/\"")
    }

    buildTypes {
        debug {
            // Local development backend on Android emulator (10.0.2.2 = localhost on host)
            // Base URL must NOT include "/api/v1/" — endpoint paths in AuthApiService and
            // OrderApiService already start with "api/v1/...". Retrofit concatenates
            // baseUrl + endpoint path, so the final URL is:
            //   http://10.0.2.2:8081/api/v1/auth/login
            buildConfigField("String", "API_BASE_URL", "\"http://10.0.2.2:8081/\"")
        }
        release {
            // Production backend — same rule: no "/api/v1/" in base URL.
            buildConfigField("String", "API_BASE_URL", "\"https://api.nearai.dev/\"")
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)

    // Core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp.core)
    implementation(libs.okhttp.logging)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // Security
    implementation(libs.androidx.security.crypto)

    // Image Loading — coil-core includes OkHttp network fetcher; coil-compose provides AsyncImage
    implementation(libs.coil.core)
    implementation(libs.coil.compose)

    // Testing — Unit (local JVM)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    testImplementation(libs.mockk)
    testImplementation(libs.robolectric)

    // Testing — Instrumented (androidTest)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)

    // Testing — Debug
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Testing — Screenshot (screenshotTest source set)
    screenshotTestImplementation(libs.androidx.compose.ui.tooling)
    screenshotTestImplementation(libs.screenshot.validation.api)
}