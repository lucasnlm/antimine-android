import com.android.build.gradle.internal.tasks.factory.dependsOn

plugins {
    id("com.android.library")
    id("kotlin-android")
}

android {
    namespace = "dev.lucasnlm.antimine.gdx"

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

    sourceSets {
        named("main") {
            jniLibs.srcDir("libs")
        }
    }
}

val natives = configurations.create("natives")

dependencies {
    implementation(project(":core"))
    implementation(project(":preferences"))
    implementation(project(":ui"))
    implementation(project(":utils"))

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity-ktx:1.8.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")

    // Koin
    implementation("io.insert-koin:koin-android:3.1.2")
    testImplementation("io.insert-koin:koin-test:3.1.2")

    // LibGDX
    api("com.badlogicgames.gdx:gdx-backend-android:1.11.0")
    api("com.badlogicgames.gdx:gdx:1.11.0")
    natives("com.badlogicgames.gdx:gdx-platform:1.11.0:natives-armeabi-v7a")
    natives("com.badlogicgames.gdx:gdx-platform:1.11.0:natives-arm64-v8a")
    natives("com.badlogicgames.gdx:gdx-platform:1.11.0:natives-x86")
    natives("com.badlogicgames.gdx:gdx-platform:1.11.0:natives-x86_64")
}

tasks.register("copyAndroidNatives") {
    doFirst {
        file("libs/armeabi-v7a/").mkdirs()
        file("libs/arm64-v8a/").mkdirs()
        file("libs/x86_64/").mkdirs()
        file("libs/x86/").mkdirs()

        natives.files.forEach { jar ->
            val outputDir: File? =
                when {
                    jar.name.endsWith("natives-arm64-v8a.jar") -> file("libs/arm64-v8a")
                    jar.name.endsWith("natives-armeabi-v7a.jar") -> file("libs/armeabi-v7a")
                    jar.name.endsWith("natives-x86_64.jar") -> file("libs/x86_64")
                    jar.name.endsWith("natives-x86.jar") -> file("libs/x86")
                    else -> null
                }

            if (outputDir != null) {
                copy {
                    from(zipTree(jar))
                    into(outputDir)
                    include("*.so")
                }
            }
        }
    }
}

//
// tasks.register("copyAndroidNatives") {
//    doFirst {

//
//        configurations.natives.files.each { jar ->

//        }
//    }
// }
//
// preBuild.dependsOn copyAndroidNatives
project.tasks.preBuild.dependsOn("copyAndroidNatives")
