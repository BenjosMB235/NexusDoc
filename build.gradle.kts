plugins {
    alias(libs.plugins.android.application) apply false
}

buildscript {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.7.1") // Mise à jour
        classpath("com.google.gms:google-services:4.4.1") // Mise à jour
        //classpath("com.google.firebase:perf-plugin:1.4.2") // Conservé (dernière version)
    }
}