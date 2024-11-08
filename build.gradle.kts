// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    application
    kotlin("jvm") version "1.9.0" //23
    kotlin("plugin.serialization") version "1.9.0" apply false
    id("org.jetbrains.dokka") version "1.9.20"
}
