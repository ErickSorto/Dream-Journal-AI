buildscript {
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.2")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.0")
        classpath("com.google.dagger:hilt-android-gradle-plugin:2.52")
        classpath("com.google.gms:google-services:4.4.2")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

plugins {
    id("com.android.application") version "8.2.2" apply false
    id("com.android.library") version "8.2.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("com.android.test") version "8.2.2" apply false
    id("androidx.baselineprofile") version "1.3.3" apply false
    id("com.google.dagger.hilt.android") version "2.49" apply false
}

tasks.register<Delete>("clean") {
    delete(rootProject.buildDir)
}
