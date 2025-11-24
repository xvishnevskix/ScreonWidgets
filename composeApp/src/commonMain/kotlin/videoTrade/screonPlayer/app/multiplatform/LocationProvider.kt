package videoTrade.screonPlayer.app.multiplatform

import androidx.compose.runtime.Composable

data class GeoPoint(
    val lat: Double,
    val lon: Double
)

interface LocationProvider {
    suspend fun getCurrentLocation(): GeoPoint?
}

@Composable
expect fun rememberPlatformLocationProvider(): LocationProvider
