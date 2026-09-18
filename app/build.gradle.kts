plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.rfaulkne.karoofueltimer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.rfaulkne.karoofueltimer3"
        minSdk = 26
        targetSdk = 29
        versionCode = 8
        versionName = "0.8"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation("io.hammerhead:karoo-ext:1.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
}
