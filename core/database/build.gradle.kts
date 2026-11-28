plugins {
    id("qurancommunity.kmp.library")
    id("qurancommunity.koin")
    id("app.cash.sqldelight")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            // UserDatabase and its queries are the module's API.
            api(libs.sqldelight.runtime)
            api(libs.koin.core) // databaseModule is public
        }
        androidMain.dependencies {
            implementation(libs.sqldelight.androidDriver)
            implementation(libs.androidx.sqlite) // the driver's SupportSQLite types
        }
        iosMain.dependencies {
            implementation(libs.sqldelight.nativeDriver)
        }
    }
}

// The reader's own data on the device (collections now; positions, settings and caches later). Migrations start at
// version 1; every schema change after the first release adds a .sqm file and is verified.
sqldelight {
    databases {
        create("UserDatabase") {
            packageName.set("app.qurancommunity.core.database")
        }
    }
}
