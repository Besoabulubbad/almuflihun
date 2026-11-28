plugins {
    id("qurancommunity.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // Use cases speak the data layer's interfaces (repositories and the Quran engine's readers).
            api(project(":core:data"))
            api(project(":core:quran")) // the Quran engine's readers (QuranReader, QuranStudy)
            api(project(":core:model"))
            api(project(":core:common"))
            api(libs.kotlinx.coroutines.core)
            api(libs.koin.core)
        }
        commonTest.dependencies {
            implementation(project(":core:testing"))
            implementation(project(":core:quran"))
            implementation(libs.kotlinx.coroutines.test)
        }
    }
}
