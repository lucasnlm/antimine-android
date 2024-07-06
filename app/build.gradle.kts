import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val isGoogleBuild: Boolean = System.getenv("IS_GOOGLE_BUILD")?.isNotBlank() == true
val isReleaseBuild: Boolean = System.getenv("IS_RELEASE_BUILD")?.isNotBlank() == true

plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.antimine"

    defaultConfig {
        // versionCode and versionName must be hardcoded to support F-droid
        versionCode = 1707001
        versionName = "17.7.0"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        compileSdk = libs.versions.compileSdk.get().toInt()
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"
    }

    signingConfigs {
        create("release") {
            if (isReleaseBuild) {
                storeFile = file("../keystore")
                keyAlias = System.getenv("BITRISEIO_ANDROID_KEYSTORE_ALIAS")
                storePassword = System.getenv("BITRISEIO_ANDROID_KEYSTORE_PASSWORD")
                keyPassword = System.getenv("BITRISEIO_ANDROID_KEYSTORE_PRIVATE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        getByName("debug") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }

        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            animationsDisabled = true
        }
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }

    flavorDimensions.add("version")
    productFlavors {
        create("google") {
            dimension = "version"
            applicationId = "dev.lucasnlm.antimine"
            versionNameSuffix = " S"

            if (isGoogleBuild) {
                plugins.apply("com.google.gms.google-services")
                plugins.apply("com.bugsnag.android.gradle")
            }
        }

        create("googleInstant") {
            versionCode = 164
            dimension = "version"
            applicationId = "dev.lucasnlm.antimine"
            versionNameSuffix = " I"

            if (isGoogleBuild) {
                plugins.apply("com.google.gms.google-services")
            }
        }

        create("auto") {
            dimension = "version"
            applicationId = "dev.lucasnlm.antimine"
            versionNameSuffix = " C"

            if (isGoogleBuild) {
                plugins.apply("com.google.gms.google-services")
                plugins.apply("com.bugsnag.android.gradle")
            }
        }

        create("foss") {
            dimension = "version"
            // There"s a typo on F-Droid release :(
            applicationId = "dev.lucanlm.antimine"
            versionNameSuffix = " F"
        }
    }

    lint {
        lintConfig = file("$rootDir/lint.xml")
    }
}

val googleImplementation by configurations
val autoImplementation by configurations
val googleInstantImplementation by configurations
val fossImplementation by configurations

dependencies {
    implementation(project(":external"))
    implementation(project(":common"))
    implementation(project(":control"))
    implementation(project(":about"))
    implementation(project(":ui"))
    implementation(project(":utils"))
    implementation(project(":preferences"))
    implementation(project(":themes"))
    implementation(project(":tutorial"))
    implementation(project(":core"))
    implementation(project(":gdx"))

    googleImplementation(project(":proprietary"))
    autoImplementation(project(":proprietary"))
    googleInstantImplementation(project(":proprietary"))
    googleInstantImplementation(project(":instant"))
    fossImplementation(project(":foss"))
    fossImplementation(project(":donation"))

    autoImplementation(project(":audio"))
    autoImplementation(project(":auto"))
    googleImplementation(project(":audio"))
    fossImplementation(project(":audio"))
    googleInstantImplementation(project(":audio-low"))

    // AndroidX
    implementation(libs.appcompat)
    implementation(libs.preference.ktx)
    implementation(libs.recyclerview)
    implementation(libs.multidex)
    implementation(libs.activity.ktx)
    implementation(libs.fragment.ktx)

    // Lifecycle
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.common.java8)

    // Constraint
    implementation(libs.constraintlayout)

    // Google
    implementation(libs.material)

    // Koin
    implementation(libs.koin.android)
    testImplementation(libs.koin.test)

    // Kotlin
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlin.stdlib)
    testImplementation(libs.kotlinx.coroutines.test)

    // Konfetti
    implementation(libs.konfetti.xml)

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.core.ktx)
    testImplementation(libs.test.core.ktx)
    testImplementation(libs.rules)
    testImplementation(libs.runner)
    testImplementation(libs.espresso.core)
    testImplementation(libs.fragment.testing)
    testImplementation(libs.robolectric)
    testImplementation(libs.ext.junit)
    testImplementation(libs.mockk)

    // Core library
    androidTestImplementation(libs.test.core)

    // AndroidJUnitRunner and JUnit Rules
    androidTestImplementation(libs.test.core)
    androidTestImplementation(libs.test.core.ktx)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.runner)
    androidTestImplementation(libs.rules)
    androidTestUtil(libs.orchestrator)
}

tasks.withType<Test>().configureEach {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }

    addTestListener(
        object : TestListener {
            override fun beforeTest(desc: TestDescriptor?) = Unit

            override fun beforeSuite(desc: TestDescriptor?) = Unit

            override fun afterSuite(
                desc: TestDescriptor,
                result: TestResult,
            ) = Unit

            override fun afterTest(
                desc: TestDescriptor,
                result: TestResult,
            ) {
                println("Executing test ${desc.name} [${desc.className}] with result: ${result.resultType}")
            }
        },
    )
}

// The following code disables Google Services when building for F-Droid
if (!isGoogleBuild) {
    android.applicationVariants.configureEach {
        if (flavorName == "foss") {
            project
                .tasks
                .names
                .map { name ->
                    name.lowercase() to project.tasks.named(name)
                }
                .filter { (name, _) ->
                    name.contains("google") || name.contains("bugsnag")
                }
                .forEach { (_, task) ->
                    task.configure {
                        enabled = false
                    }
                }
        }
    }
}
