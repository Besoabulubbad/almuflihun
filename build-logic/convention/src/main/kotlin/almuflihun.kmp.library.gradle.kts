// Kotlin Multiplatform library: Android (AGP KMP library plugin) + iOS device and simulator.
// Every core:* and feature:* module starts here (docs/architecture/modules.md).
import app.almuflihun.buildlogic.libs
import app.almuflihun.buildlogic.library
import app.almuflihun.buildlogic.almuflihunNamespace
import app.almuflihun.buildlogic.version
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("org.jetbrains.kotlin.multiplatform")
    id("com.android.kotlin.multiplatform.library")
    id("almuflihun.detekt")
    // dependency-analysis 3.x is applied per module (the root only aggregates `buildHealth`).
    id("com.autonomousapps.dependency-analysis")
}

kotlin {
    iosArm64()
    iosSimulatorArm64()

    android {
        namespace = almuflihunNamespace
        compileSdk = libs.version("android-compileSdk").toInt()
        minSdk = libs.version("android-minSdk").toInt()
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        commonTest.dependencies {
            implementation(libs.library("kotlin-test"))
        }
    }
}
