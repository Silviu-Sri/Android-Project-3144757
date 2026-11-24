package com.griffith.androidproject_3144757.pages

import androidx.annotation.StringRes
import com.griffith.androidproject_3144757.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

data class Weather(
    val temperatureCelsius: Double,
    val isDay: Boolean,
    val condition: WeatherCondition,
    val localTime: String
)

enum class WeatherCondition(@param:StringRes val label: Int) {
    CLEAR(R.string.weather_clear),
    PARTLY_CLOUDY(R.string.weather_partly_cloudy),
    CLOUDY(R.string.weather_cloudy),
    FOG(R.string.weather_fog),
    DRIZZLE(R.string.weather_drizzle),
    RAIN(R.string.weather_rain),
    FREEZING_RAIN(R.string.weather_freezing_rain),
    SNOW(R.string.weather_snow),
    THUNDERSTORM(R.string.weather_thunderstorm),
    UNKNOWN(R.string.weather_unknown);

    companion object {
        fun fromCode(code: Int): WeatherCondition = when (code) {
            0, 1 -> CLEAR
            2 -> PARTLY_CLOUDY
            3 -> CLOUDY
            45, 48 -> FOG
            51, 53, 55 -> DRIZZLE
            56, 57, 66, 67 -> FREEZING_RAIN
            61, 63, 65, 80, 81, 82 -> RAIN
            71, 73, 75, 77, 85, 86 -> SNOW
            95, 96, 97, 99 -> THUNDERSTORM
            else -> UNKNOWN
        }
    }
}

object WeatherRepository {
    suspend fun fetch(latitude: Double, longitude: Double): Weather = withContext(Dispatchers.IO) {
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
        val endpoint = "https://api.open-meteo.com/v1/forecast" +
            "?latitude=$latitude&longitude=$longitude" +
            "&current=temperature_2m,is_day,weather_code" +
            "&temperature_unit=celsius&timezone=auto"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IOException("Weather service returned HTTP ${connection.responseCode}")
            }
            val json = connection.inputStream.bufferedReader().use { JSONObject(it.readText()) }
            val current = json.getJSONObject("current")
            val temperature = current.getDouble("temperature_2m")
            val daylight = current.getInt("is_day")
            if (!temperature.isFinite() || daylight !in 0..1) {
                throw IOException("Invalid current weather")
            }
            Weather(
                temperatureCelsius = temperature,
                isDay = daylight == 1,
                condition = WeatherCondition.fromCode(current.getInt("weather_code")),
                localTime = current.getString("time")
            )
        } finally {
            connection.disconnect()
        }
    }
}
