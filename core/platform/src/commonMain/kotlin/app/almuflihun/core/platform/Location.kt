package app.almuflihun.core.platform

import androidx.compose.runtime.Composable
import app.almuflihun.core.common.AppResult

/** A point on Earth, in degrees. Approximate by design: the app never asks for or keeps a precise position. */
data class GeoPoint(val latitude: Double, val longitude: Double)

/** Whether the app may read the approximate location. */
enum class LocationPermission { Granted, Denied, NotAsked }

/**
 * The approximate location for moonrise, moonset, sun times and «أوقات التسبيح» (docs/specs/screens.md §12). Only
 * coarse precision is ever requested (Android `ACCESS_COARSE_LOCATION`, iOS kilometre accuracy), the result is used
 * on the device only, and nothing is sent anywhere. Without permission the app uses the city the user picked.
 */
interface LocationProvider {
    fun permission(): LocationPermission

    /** One fresh approximate fix, or `AppError.PermissionDenied` / `AppError.Offline`-free failures. */
    suspend fun currentApproximate(): AppResult<GeoPoint>
}

/** Asks for the location permission from a screen (Onboarding, Settings). Holds the latest answer. */
interface LocationPermissionRequester {
    val permission: LocationPermission
    fun request()
}

/** Remembers a [LocationPermissionRequester] that calls [onResult] after the system dialog closes. */
@Composable
expect fun rememberLocationPermissionRequester(onResult: (LocationPermission) -> Unit): LocationPermissionRequester
