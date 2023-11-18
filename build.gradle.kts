buildscript {
    dependencies {
        if (System.getenv("IS_GOOGLE_BUILD") == "1") {
            classpath("com.bugsnag:bugsnag-android-gradle-plugin:8.1.0")
        }
    }
}

plugins {
    alias(libs.plugins.android) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false

    if (System.getenv("IS_GOOGLE_BUILD")?.isNotBlank() == true) {
        alias(libs.plugins.google.services) apply false
    }
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
