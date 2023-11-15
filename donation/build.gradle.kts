plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.antimine.donation"

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

    implementation(project(":core"))
    implementation(project(":external"))
    implementation(project(":i18n"))
    implementation(project(":preferences"))
    implementation(project(":ui"))
    implementation(project(":utils"))

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Google
    implementation("com.google.android.material:material:1.10.0")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.3.2")

    // Constraint
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")

    // Koin
    implementation("io.insert-koin:koin-android:3.1.2")
    testImplementation("io.insert-koin:koin-test:3.1.2")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")

    // Kotlin Lib
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")

    // Unit Tests
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:4.6.1")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.1.0")
    testImplementation("io.mockk:mockk:1.13.5")
}
