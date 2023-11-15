plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.antimine.instant"

    defaultConfig {
        minSdk = 21
        compileSdk = 34
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
