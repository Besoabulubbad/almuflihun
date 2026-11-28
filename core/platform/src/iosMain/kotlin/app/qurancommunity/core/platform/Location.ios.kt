package app.qurancommunity.core.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import app.qurancommunity.core.common.AppError
import app.qurancommunity.core.common.AppResult
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.CoreLocation.kCLLocationAccuracyKilometer
import platform.Foundation.NSError
import platform.darwin.NSObject
import kotlin.coroutines.resume

private fun iosPermission(manager: CLLocationManager): LocationPermission = when (manager.authorizationStatus) {
    kCLAuthorizationStatusAuthorizedWhenInUse, kCLAuthorizationStatusAuthorizedAlways -> LocationPermission.Granted
    kCLAuthorizationStatusDenied, kCLAuthorizationStatusRestricted -> LocationPermission.Denied
    else -> LocationPermission.NotAsked
}

/** Kilometre accuracy through CoreLocation; one fix per call. */
@OptIn(ExperimentalForeignApi::class)
internal class IosLocationProvider : LocationProvider {

    private val manager = CLLocationManager().apply { desiredAccuracy = kCLLocationAccuracyKilometer }
    private var delegate: OneShotDelegate? = null

    override fun permission(): LocationPermission = iosPermission(manager)

    override suspend fun currentApproximate(): AppResult<GeoPoint> {
        if (permission() != LocationPermission.Granted) return AppResult.Failure(AppError.PermissionDenied)
        return suspendCancellableCoroutine { continuation ->
            val oneShot = OneShotDelegate { result -> if (continuation.isActive) continuation.resume(result) }
            delegate = oneShot
            manager.delegate = oneShot
            manager.requestLocation()
        }
    }

    private class OneShotDelegate(private val done: (AppResult<GeoPoint>) -> Unit) :
        NSObject(), CLLocationManagerDelegateProtocol {

        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation
            if (location == null) {
                done(AppResult.Failure(AppError.Unexpected()))
                return
            }
            location.coordinate.useContents { done(AppResult.Success(GeoPoint(latitude, longitude))) }
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: NSError) {
            done(AppResult.Failure(AppError.Unexpected()))
        }
    }
}

@Composable
actual fun rememberLocationPermissionRequester(onResult: (LocationPermission) -> Unit): LocationPermissionRequester {
    val manager = remember { CLLocationManager() }
    val state = remember { IosPermissionState(iosPermission(manager)) }
    val delegate = remember {
        AuthorizationDelegate { status ->
            state.permission = status
            onResult(status)
        }
    }
    return remember {
        manager.delegate = delegate
        object : LocationPermissionRequester {
            override val permission: LocationPermission get() = state.permission
            override fun request() = manager.requestWhenInUseAuthorization()
        }
    }
}

private class IosPermissionState(initial: LocationPermission) {
    var permission by mutableStateOf(initial)
}

private class AuthorizationDelegate(private val changed: (LocationPermission) -> Unit) :
    NSObject(), CLLocationManagerDelegateProtocol {
    override fun locationManagerDidChangeAuthorization(manager: CLLocationManager) {
        changed(iosPermission(manager))
    }
}
