buildscript {
    dependencies {
        classpath(libs.gradle)
        classpath(libs.kotlin.gradle.plugin)
        classpath(libs.google.services)
    }
}

plugins {
    id("com.android.library") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "2.2.21" apply false
    id("com.android.test") version "8.13.1" apply false
    id("androidx.baselineprofile") version "1.4.1" apply false

    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinSerialization) apply false
    alias(libs.plugins.googleServices) apply false
    alias(libs.plugins.kotlinCocoapods) apply false
    alias(libs.plugins.stability.analyzer) apply false

}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
