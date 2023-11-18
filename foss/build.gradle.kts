plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.foss"

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
    implementation(project(":i18n"))

    // Kotlin
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlinx.coroutines.test)

    // Koin
    implementation(libs.koin.android)
    testImplementation(libs.koin.test)

    // Acra
    implementation(libs.acra.core)
    implementation(libs.acra.mail)
    implementation(libs.acra.toast)
    implementation(libs.acra.limiter)
}
