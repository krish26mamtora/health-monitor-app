plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.example.connectwatch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.connectwatch"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
//        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            MinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}
kotlin {
    jvmToolchain(17) // Or your desired Java version (e.g., 11)
}
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
}
