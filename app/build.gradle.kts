import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services") // Firebase
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    // Keep this at last (https://stackoverflow.com/questions/70550883/warning-the-following-options-were-not-recognized-by-any-processor-dagger-f)
    id("com.google.devtools.ksp")
    id("kotlin-kapt")
}

// Load secrets from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { stream: FileInputStream ->
        localProperties.load(stream)
    }
}

android {
    namespace = "com.github.zzorgg.beezle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.github.zzorgg.beezle"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Add secrets as BuildConfig fields
        buildConfigField("String", "FIREBASE_DATABASE_URL", "\"${localProperties.getProperty("FIREBASE_DATABASE_URL", "")}\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${localProperties.getProperty("FIREBASE_API_KEY", "")}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${localProperties.getProperty("FIREBASE_PROJECT_ID", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Read from local.properties
            buildConfigField(
                "String",
                "WEBSOCKET_URL",
                "\"${localProperties.getProperty("WEBSOCKET_URL", "wss://octopus-app-4x8aa.ondigitalocean.app/ws")}\""
            )
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            // Read from local.properties
            buildConfigField(
                "String",
                "WEBSOCKET_URL",
                "\"${localProperties.getProperty("WEBSOCKET_URL", "wss://octopus-app-4x8aa.ondigitalocean.app/ws")}\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        buildConfig = true // Enable BuildConfig so WEBSOCKET_URL field is generated
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    // Material Icons Extended for more icons
    implementation(libs.androidx.compose.material.icons.extended)
    // Google Fonts in Compose (optional)
    implementation(libs.androidx.compose.ui.text.google.fonts)

    // Material Components for Android (provides Theme.Material3.* XML themes)
    implementation(libs.material)

    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Lottie animations
    implementation(libs.lottie.compose)

    // Coil for image loading
    implementation(libs.coil.compose)
    implementation(libs.coil.gif)
    implementation(libs.coil.svg)

    // Solana Mobile Stack dependencies - Simplified approach
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.1.0")

    // Basic encoders for Solana addresses
    // implementation("org.bitcoinj:bitcoinj-core:0.17")

    // Additional coroutines support for async operations
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // WebSocket support for duel functionality
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // JSON serialization for WebSocket messages
    implementation(libs.kotlinx.serialization.json)

    // Additional UI enhancements
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.graphics)

    // DataStore for persisting wallet connection
    implementation(libs.androidx.datastore.preferences)

    // Hilt navigation for Compose
    implementation(libs.androidx.hilt.navigation.compose)

    // Firebase BOM to manage versions
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-database") // Add Firebase Realtime Database

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
}
