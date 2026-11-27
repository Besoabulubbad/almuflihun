package app.almuflihun.core.testing

import app.almuflihun.core.common.AppError
import app.almuflihun.core.common.AppResult
import app.almuflihun.core.platform.GeoPoint
import app.almuflihun.core.platform.LocationPermission
import app.almuflihun.core.platform.LocationProvider
import app.almuflihun.core.platform.MotionSettings
import app.almuflihun.core.platform.ShareSheet

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
