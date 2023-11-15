plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.antimine.proprietary"

    defaultConfig {
        minSdk = 21
        compileSdk = 34

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Dependencies must be hardcoded to support F-droid

    implementation(project(":external"))

    // Amplitude
    implementation("com.amplitude:android-sdk:2.32.1")
    implementation("com.squareup.okhttp3:okhttp:4.10.0")

    // Bugsnag
    implementation("com.bugsnag:bugsnag-android:5.31.2")

    // Google
    implementation("com.android.billingclient:billing-ktx:6.0.1")
    implementation("com.google.android.gms:play-services-instantapps:18.0.1")
    implementation("com.google.android.gms:play-services-games:23.1.0")
    implementation("com.google.android.gms:play-services-auth:20.7.0")
    implementation("com.google.android.gms:play-services-ads:22.4.0")
    implementation("com.google.android.play:core-ktx:1.8.1")

    // Jetbrains
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Kotlin
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Koin
    implementation("io.insert-koin:koin-android:3.1.2")
    testImplementation("io.insert-koin:koin-test:3.1.2")
}
