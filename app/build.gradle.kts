plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
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
    implementation("com.google.android.material:material:1.12.0")

    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Lottie animations
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-gif:2.4.0")
    implementation("io.coil-kt:coil-svg:2.4.0")

    // Solana Mobile Stack dependencies - Simplified approach
    implementation("com.solanamobile:mobile-wallet-adapter-clientlib-ktx:2.1.0")

    // Basic encoders for Solana addresses
    implementation("org.bitcoinj:bitcoinj-core:0.16.1")

    // Additional coroutines support for async operations
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Additional UI enhancements
    implementation("androidx.compose.animation:animation:1.5.4")
    implementation("androidx.compose.animation:animation-graphics:1.5.4")
}