plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    // Keep this at last (https://stackoverflow.com/questions/70550883/warning-the-following-options-were-not-recognized-by-any-processor-dagger-f)
    id("kotlin-kapt")
    id("com.google.gms.google-services") // Firebase
}

android {
    namespace = "com.example.beezle"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.beezle"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // In production you likely want secure wss:// endpoint
            buildConfigField("String", "WEBSOCKET_URL", "\"wss://your.production.host/ws\"")
        }
        debug {
            // Use emulator host alias instead of localhost
            buildConfigField("String", "WEBSOCKET_URL", "\"ws://10.0.2.2:8080/ws\"")
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

// Exclude legacy protolite that collides with newer protobuf-javalite (duplicate DescriptorProtos classes)
configurations.all {
    exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:4.29.3")
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
    implementation("androidx.compose.material:material-icons-extended")
    // Google Fonts in Compose (optional)
    implementation("androidx.compose.ui:ui-text-google-fonts")

    // Material Components for Android (provides Theme.Material3.* XML themes)
    implementation("com.google.android.material:material:1.13.0")

    implementation("androidx.navigation:navigation-compose:2.9.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Lottie animations
    implementation("com.airbnb.android:lottie-compose:6.6.9")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil-gif:2.7.0")
    implementation("io.coil-kt:coil-svg:2.4.0")

    // Solana Mobile Stack dependencies - Simplified approach
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.1.0")

    // Basic encoders for Solana addresses
    implementation("org.bitcoinj:bitcoinj-core:0.17")

    // Additional coroutines support for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.10.2")

    // WebSocket support for duel functionality
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // JSON serialization for WebSocket messages
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Additional UI enhancements
    implementation("androidx.compose.animation:animation:1.9.2")
    implementation("androidx.compose.animation:animation-graphics:1.9.2")

    // DataStore for persisting wallet connection
    implementation("androidx.datastore:datastore-preferences:1.1.7")

    // Hilt navigation for Compose
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    // Firebase BOM to manage versions
    implementation(platform("com.google.firebase:firebase-bom:33.4.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx") {
        exclude(group = "com.google.firebase", module = "protolite-well-known-types")
    }
    // Also add the dependencies for the Credential Manager libraries and specify their versions
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    implementation("com.google.dagger:hilt-android:2.57.2")
    ksp("com.google.dagger:hilt-android-compiler:2.57.2")

    // Explicit protobuf runtime & common lite protos (provides google.type.LatLng) after excluding protolite
    implementation("com.google.protobuf:protobuf-javalite:4.29.3")
    implementation("com.google.api.grpc:proto-google-common-protos:2.17.0") {
        exclude(group = "com.google.protobuf", module = "protobuf-java")
    }
}
