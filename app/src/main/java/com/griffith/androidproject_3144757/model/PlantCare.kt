package com.griffith.androidproject_3144757.model

import com.griffith.androidproject_3144757.pages.Weather
import com.griffith.androidproject_3144757.pages.WeatherCondition
import kotlin.math.min

object PlantCare {
    fun advance(state: PlantState, weather: Weather?, now: Long): PlantState {
        require(now >= 0L)
        if (now <= state.lastUpdatedEpochMillis) return state
        var remaining = min(now - state.lastUpdatedEpochMillis, 24 * 3_600_000L)
        var moisture = state.soilMoisturePercent
        var health = state.healthPercent
        var growth = state.growthProgressPercent
        var happiness = state.happinessPercent
        while (remaining > 0L) {
            val step = min(remaining, 60_000L)
            val hours = step / 3_600_000f
            val temperature = weather?.temperatureCelsius ?: 18.0
            val drying = (if (weather?.isDay == true) 2f else 1f) +
                ((temperature - 20.0).coerceAtLeast(0.0) * 0.1).toFloat()
            val rain = when (weather?.condition) {
                WeatherCondition.RAIN, WeatherCondition.THUNDERSTORM -> 5f
                WeatherCondition.DRIZZLE, WeatherCondition.FREEZING_RAIN -> 2f
                else -> 0f
            }
            moisture = (moisture + (rain - drying) * hours).coerceIn(0f, 100f)
            val comfortable = moisture in 30f..80f && temperature in 5.0..35.0
            health = (health + (if (comfortable) 2f else -1f) * hours).coerceIn(20f, 100f)
            happiness = (happiness + (if (comfortable && health >= 50f) 1f else -0.5f) * hours)
                .coerceIn(20f, 100f)
            if (comfortable && health >= 50f) {
                growth = (growth + hours * (100f / 168f)).coerceAtMost(100f)
            }
            remaining -= step
        }
        return state.copy(
            soilMoisturePercent = moisture,
            healthPercent = health,
            growthProgressPercent = growth,
            lastUpdatedEpochMillis = now,
            happinessPercent = happiness
        )
    }

    fun water(state: PlantState): PlantState =
        state.copy(soilMoisturePercent = (state.soilMoisturePercent + 20f).coerceAtMost(100f))
}
