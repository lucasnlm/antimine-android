plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.antimine.proprietary"

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            setProguardFiles(listOf(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    implementation(project(":external"))

    // Amplitude
    implementation(libs.amplitude.sdk)
    implementation(libs.okhttp)

    // Bugsnag
    implementation(libs.bugsnag.android)

    // Google
    implementation(libs.google.billing.ktx)
    implementation(libs.play.services.instantapps)
    implementation(libs.play.services.games)
    implementation(libs.play.services.auth)
    implementation(libs.play.services.ads)
    implementation(libs.play.core.ktx)

    // Jetbrains
    implementation(libs.kotlinx.coroutines.play.services)

    // Kotlin
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlinx.coroutines.test)

    // Koin
    implementation(libs.koin.android)
    testImplementation(libs.koin.test)
}
