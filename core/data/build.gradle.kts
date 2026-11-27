import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    id("almuflihun.kmp.library")
    id("org.jetbrains.kotlin.plugin.serialization")
}

kotlin {
    // The iOS test binaries use SQLDelight's native driver through core:database, which needs the system SQLite.
    targets.withType<KotlinNativeTarget>().configureEach {
        binaries.all { linkerOpts("-lsqlite3") }
    }
    compilerOptions {
        // PlatformTest is an expect class: the tests share one Robolectric runner on Android.
        freeCompilerArgs.add("-Xexpect-actual-classes")
    }
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:common"))
            implementation(project(":core:database"))
            api(project(":core:quran")) // SearchHistoryRepository speaks SearchQuery
            implementation(libs.kotlinx.serialization.json) // the stored search queries
            implementation(libs.sqldelight.coroutines)
            implementation(libs.sqldelight.runtime) // Query and transactions
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.kotlinx.datetime) // FakeClock's TimeZone
        }
        androidMain.dependencies {
            implementation(libs.kotlinx.serialization.core) // the generated serializers' runtime types
        }
        named("androidHostTest").dependencies {
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.sqldelight.runtime)
            implementation(libs.androidx.sqlite)
            implementation(libs.robolectric.annotations)
            implementation(libs.robolectric.shadowsFramework) // RuntimeEnvironment
            implementation(libs.robolectric)
            implementation(libs.junit)
        }
        named("androidDeviceTest").dependencies {
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.sqldelight.runtime)
            implementation(libs.androidx.sqlite)
            implementation(libs.androidx.testExt.junit)
            implementation(libs.androidx.testMonitor)
            runtimeOnly(libs.androidx.testRunner) // AndroidJUnitRunner
            implementation(libs.junit)
        }
        iosTest.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }
    }
}
