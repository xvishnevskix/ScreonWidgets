package videoTrade.screonPlayer.app.presentation.components.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import videoTrade.screonPlayer.app.domain.model.windgets.WidgetDescriptor
import videoTrade.screonPlayer.app.multiplatform.GeoPoint
import videoTrade.screonPlayer.app.multiplatform.LocationProvider
import videoTrade.screonPlayer.app.multiplatform.rememberPlatformLocationProvider

@Composable
fun WeatherWidget(
    descriptor: WidgetDescriptor,
    modifier: Modifier = Modifier,
    locationProvider: LocationProvider = rememberPlatformLocationProvider()
) {
    val payloadLat = descriptor.payload["lat"]?.toDoubleOrNull()
    val payloadLon = descriptor.payload["lon"]?.toDoubleOrNull()
    val payloadCity = descriptor.payload["city"]

    var deviceLocation by remember { mutableStateOf<GeoPoint?>(null) }

    // Если в payload нет координат, пробуем взять локальные
    LaunchedEffect(descriptor.id) {
        if (payloadLat == null || payloadLon == null) {
            deviceLocation = try {
                locationProvider.getCurrentLocation()
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Итоговые координаты
    val lat = payloadLat ?: deviceLocation?.lat
    val lon = payloadLon ?: deviceLocation?.lon

    val city = when {
        payloadCity != null -> payloadCity
        deviceLocation != null -> "Текущее местоположение"
        else -> "Город"
    }

    var tempText by remember { mutableStateOf("--°") }
    var descText by remember { mutableStateOf("Загрузка...") }
    var iconText by remember { mutableStateOf("⏳") }

    // При наличии координат грузим погоду
    LaunchedEffect(descriptor.id, lat, lon) {
        if (lat == null || lon == null) {
            descText = "Нет координат"
            tempText = "--°"
            iconText = "⚠️"
            return@LaunchedEffect
        }

        val client = HttpClient()
        try {
            val response = withContext(Dispatchers.IO) {
                client.get(
                    "https://api.open-meteo.com/v1/forecast" +
                            "?latitude=$lat&longitude=$lon&current_weather=true"
                ).body<String>()
            }

            val json = Json { ignoreUnknownKeys = true }
            val parsed = json.decodeFromString(OpenMeteoResponse.serializer(), response)
            val cw = parsed.current_weather

            if (cw != null) {
                tempText = "${cw.temperature.toInt()}°"
                val ui = weatherCodeToUi(cw.weathercode)
                descText = ui.description
                iconText = ui.icon
            } else {
                descText = "Нет данных"
                iconText = "❔"
            }
        } catch (e: Exception) {
            e.printStackTrace()
            descText = "Ошибка загрузки"
            tempText = "--°"
            iconText = "⚠️"
        } finally {
            client.close()
        }
    }

    // UI
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xCC111827),
                            Color(0xCC020617)
                        )
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = iconText,
                    fontSize = 26.sp
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f)
                ) {



                    Text(
                        text = city,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFF9FAFB)
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = tempText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFBFDBFE)
                        )
                        Text(
                            text = descText,
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                }
            }
        }
    }
}

data class WeatherUi(
    val description: String,
    val icon: String
)

fun weatherCodeToUi(code: Int): WeatherUi = when (code) {
    0 -> WeatherUi("Ясно", "☀️")
    1, 2 -> WeatherUi("Преимущественно ясно", "🌤️")
    3 -> WeatherUi("Облачно", "☁️")
    in 45..48 -> WeatherUi("Туман", "🌫️")
    in 51..57 -> WeatherUi("Морось", "🌦️")
    in 61..67 -> WeatherUi("Дождь", "🌧️")
    in 71..77 -> WeatherUi("Снег", "🌨️")
    in 80..82 -> WeatherUi("Ливни", "🌧️")
    in 95..99 -> WeatherUi("Гроза", "⛈️")
    else -> WeatherUi("Погода", "🌡️")
}

@Serializable
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    val current_weather: OpenMeteoCurrentWeather? = null
)

@Serializable
data class OpenMeteoCurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int
)