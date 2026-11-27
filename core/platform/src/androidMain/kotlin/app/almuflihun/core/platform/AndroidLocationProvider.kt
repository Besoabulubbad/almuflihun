package app.almuflihun.core.platform

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.core.os.CancellationSignal
import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/** Coarse location through the platform `LocationManager`: no Google Play Services, so it works on every phone. */
internal class AndroidLocationProvider(private val context: Context) : LocationProvider {

    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    override fun permission(): LocationPermission = androidPermission(context)

    @SuppressLint("MissingPermission") // checked just above each call
    override suspend fun currentApproximate(): AppResult<GeoPoint> {
        if (permission() != LocationPermission.Granted) return AppResult.Failure(AppError.PermissionDenied)
        val provider = listOf(LocationManager.NETWORK_PROVIDER, LocationManager.PASSIVE_PROVIDER)
            .firstOrNull { it in manager.allProviders && manager.isProviderEnabled(it) }
            ?: return AppResult.Failure(AppError.Unexpected())
        return suspendCancellableCoroutine { continuation ->
            val signal = CancellationSignal()
            continuation.invokeOnCancellation { signal.cancel() }
            LocationManagerCompat.getCurrentLocation(
                manager,
                provider,
                signal,
                ContextCompat.getMainExecutor(context),
            ) { location ->
                val result: AppResult<GeoPoint> = if (location != null) {
                    AppResult.Success(GeoPoint(location.latitude, location.longitude))
                } else {
                    AppResult.Failure(AppError.Unexpected())
                }
                continuation.resume(result)
            }
        }
    }
}

private fun androidPermission(context: Context): LocationPermission =
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED
    ) {
        LocationPermission.Granted
    } else {
        LocationPermission.NotAsked
    }

@Composable
actual fun rememberLocationPermissionRequester(onResult: (LocationPermission) -> Unit): LocationPermissionRequester {
    val context = LocalContext.current
    val state = remember { AndroidPermissionState(androidPermission(context)) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        state.permission = if (granted) LocationPermission.Granted else LocationPermission.Denied
        onResult(state.permission)
    }
    return remember(launcher) {
        object : LocationPermissionRequester {
            override val permission: LocationPermission get() = state.permission
            override fun request() = launcher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
        }
    }
}

private class AndroidPermissionState(initial: LocationPermission) {
    var permission by mutableStateOf(initial)
}
