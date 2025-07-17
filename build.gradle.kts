// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
}
buildscript {
    repositories {
        google()  // Dépôt pour les dépendances Google
        mavenCentral()  // Dépôt Maven central
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.0") // Vérifiez votre version Gradle
        classpath ("com.google.gms:google-services:4.3.15")
    }
}