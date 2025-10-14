import java.io.FileInputStream
import java.util.Properties

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

val localProperties = Properties()
val localPropertiesFile = File(rootDir, "local.properties")
if (localPropertiesFile.exists() && localPropertiesFile.isFile) {
    localPropertiesFile.inputStream().let {
        localProperties.load(it)
    }
} else {
    throw IllegalStateException(
        "Missing configuration file: 'local.properties'.\n"
                + "Please create this file in the project root and define required keys.n"
    )
}

val localPropertiesRequiredKeys = listOf("WEBSOCKET_URL")

val missingKeys = localPropertiesRequiredKeys.filterNot { localProperties.containsKey(it) }
if (missingKeys.isNotEmpty()) {
    throw IllegalStateException(
        "Missing required key(s) in local.properties: ${missingKeys.joinToString(", ")}"
    )
}

// https://developer.android.com/studio/publish/app-signing#secure-shared-keystore
var keystoreProperties: Properties? = null
val keystorePropertiesFile = File(rootDir, "keystore.properties")
if (keystorePropertiesFile.exists() && keystorePropertiesFile.isFile) {
    keystoreProperties = Properties()
    keystoreProperties?.load(FileInputStream(keystorePropertiesFile))
}


android {
    namespace = "com.github.zzorgg.beezle"
    compileSdk = 36

    keystoreProperties?.let { keystore ->
        signingConfigs {
            create("beezle-config") {
                keyAlias = keystore["KEY_ALIAS"] as String
                keyPassword = keystore["KEY_PASS"] as String
                storeFile = file(keystore["KEYSTORE_FILE"] as String)
                storePassword = keystore["KEYSTORE_PASS"] as String
            }
        }
    }

    defaultConfig {
        applicationId = "com.github.zzorgg.beezle"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        localPropertiesRequiredKeys.onEach {
            buildConfigField(
                "String",
                it,
                "\"${localProperties[it] as String}\""
            )
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
            if (keystoreProperties != null) {
                signingConfig = signingConfigs["beezle-config"]
            }
        }
        debug {
            isMinifyEnabled = false
            isShrinkResources = false
            // Having this commonly in defaultConfig doesn't work for debug somehow
            if (keystoreProperties != null) {
                signingConfig = signingConfigs["beezle-config"]
            }
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

    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)

    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
}
