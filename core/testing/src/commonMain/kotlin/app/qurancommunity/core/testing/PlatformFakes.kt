package app.qurancommunity.core.testing

import app.qurancommunity.core.common.AppError
import app.qurancommunity.core.common.AppResult
import app.qurancommunity.core.platform.GeoPoint
import app.qurancommunity.core.platform.LocationPermission
import app.qurancommunity.core.platform.LocationProvider
import app.qurancommunity.core.platform.MotionSettings
import app.qurancommunity.core.platform.ShareSheet

/** Records what was shared, for ViewModel and screen tests. */
class FakeShareSheet : ShareSheet {
    val shared = mutableListOf<String>()
    override fun shareText(text: String) {
        shared += text
    }
}

/** A fixed reduce-motion answer for tests and screenshots. */
class FakeMotionSettings(override val reduceMotion: Boolean = false) : MotionSettings

/** A location provider with a fixed permission and fix (Jerash by default, as in the design mockups). */
class FakeLocationProvider(
    var permissionState: LocationPermission = LocationPermission.Granted,
    var fix: GeoPoint = GeoPoint(latitude = 32.28, longitude = 35.90),
) : LocationProvider {
    override fun permission(): LocationPermission = permissionState

    override suspend fun currentApproximate(): AppResult<GeoPoint> =
        if (permissionState == LocationPermission.Granted) {
            AppResult.Success(fix)
        } else {
            AppResult.Failure(AppError.PermissionDenied)
        }
}
