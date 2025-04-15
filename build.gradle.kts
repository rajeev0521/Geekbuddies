// Top-level build file where you can add configuration options common to all sub-projects/modules.

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
}

buildscript {

    dependencies {
        classpath("com.android.tools.build:gradle:7.4.0") // Replace with the appropriate version
        classpath("com.google.gms:google-services:4.4.1")
    }
}

allprojects {
}
