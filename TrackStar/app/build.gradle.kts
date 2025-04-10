plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.trackstar"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.trackstar"
        minSdk = 24
        targetSdk = 35
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
    // Core Libraries
    implementation("androidx.core:core-ktx:1.8.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")
    implementation("androidx.activity:activity-compose:1.5.0")

    // Compose UI Libraries
    implementation(platform("androidx.compose:compose-bom:2022.10.00"))
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-graphics:1.2.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.2.0")
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.runtime:runtime-livedata:1.5.0")

    // Testing Libraries
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.4")
    androidTestImplementation("androidx.espresso:espresso-core:3.5.0")
    androidTestImplementation(platform("androidx.compose:compose-bom:2022.10.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // Use latest version

// Debug Libraries for Compose
    debugImplementation("androidx.compose.ui:ui-tooling:1.2.0")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.2.0")
}