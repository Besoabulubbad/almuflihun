rootProject.name = "almuflihun"

pluginManagement {
    includeBuild("build-logic")
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

// Application shells
include(":app:android")
include(":app:shared")

// Core layers and features (docs/architecture/modules.md for the dependency contract)
include(":core:model")
include(":core:common")
include(":core:localization")
include(":core:designsystem")
include(":core:ui")
include(":core:navigation")
include(":core:platform")
include(":core:quran")
include(":core:astronomy")
include(":core:audio")
include(":core:database")
include(":core:firebase")
include(":core:analytics")
include(":core:update")
include(":core:data")
include(":core:domain")
include(":core:testing")

// Features
include(":feature:home")
include(":feature:moon")
include(":feature:quran-hub")
include(":feature:mushaf")
include(":feature:hizb")
include(":feature:doors")
include(":feature:diyaa")
include(":feature:collections")
include(":feature:live")
include(":feature:community")
include(":feature:profile")
include(":feature:admin")
include(":feature:settings")

// Tooling
include(":tooling:konsist")
include(":tooling:detekt-rules")
