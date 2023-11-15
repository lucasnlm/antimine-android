buildscript {
    extra.apply{
        set("isGoogleBuild", System.getenv("IS_GOOGLE_BUILD")?.isNotBlank() == true)
        set("isReleaseBuild", System.getenv("IS_RELEASE_BUILD")?.isNotBlank() == true)
    }

    dependencies {
        if (System.getenv("IS_GOOGLE_BUILD") == "1") {
            classpath("com.bugsnag:bugsnag-android-gradle-plugin:8.1.0")
        }
    }
}

plugins {
    id("com.android.application") version "8.1.3" apply false
    id("com.android.library") version "8.1.3" apply false
    id("org.jetbrains.kotlin.android") version "1.9.10" apply false
    id("com.google.gms.google-services") version "4.4.0" apply false
}

allprojects {
    repositories {
        mavenCentral()
        google()
    }
}
