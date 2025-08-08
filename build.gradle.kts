// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    val kotlinVersion = "2.0.0"
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) version kotlinVersion apply false
//    id("com.android.application") version "8.5.2" apply false
//    id("org.jetbrains.kotlin.android") version kotlinVersion apply false
    id("com.google.dagger.hilt.android") version "2.51.1" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("com.google.firebase.crashlytics") version "3.0.2" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21" apply false
    kotlin("jvm") version kotlinVersion
//    alias(libs.hilt.android.gradle.plugin) apply false
}

buildscript{
    repositories {
        google()  // Google's Maven repository
    }
    dependencies{
        classpath(libs.hilt.android.gradle.plugin)
    }
}
