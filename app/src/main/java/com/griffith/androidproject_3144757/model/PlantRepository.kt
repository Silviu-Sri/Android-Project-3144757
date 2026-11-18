package com.griffith.androidproject_3144757.model

import android.content.Context
import com.griffith.androidproject_3144757.pages.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class PlantRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("plant", Context.MODE_PRIVATE)

    suspend fun update(weather: Weather?, now: Long = System.currentTimeMillis()): PlantState =
        withContext(Dispatchers.IO) {
            val saved = load(now)
            val updated = PlantCare.advance(saved, weather, now)
            save(updated)
            updated
        }

    suspend fun read(): PlantState = withContext(Dispatchers.IO) {
        load(System.currentTimeMillis())
    }

    private fun load(now: Long): PlantState {
        if (!preferences.contains("updated")) return PlantState(lastUpdatedEpochMillis = now)
        return try {
            PlantState(
                preferences.getFloat("moisture", 60f),
                preferences.getFloat("health", 100f),
                preferences.getFloat("growth", 0f),
                preferences.getLong("updated", now),
                preferences.getFloat("happiness", 80f)
            )
        } catch (_: IllegalArgumentException) {
            PlantState(lastUpdatedEpochMillis = now)
        } catch (_: ClassCastException) {
            PlantState(lastUpdatedEpochMillis = now)
        }
    }

    private fun save(state: PlantState) {
        val saved = preferences.edit()
            .putFloat("moisture", state.soilMoisturePercent)
            .putFloat("health", state.healthPercent)
            .putFloat("growth", state.growthProgressPercent)
            .putFloat("happiness", state.happinessPercent)
            .putLong("updated", state.lastUpdatedEpochMillis)
            .commit()
        if (!saved) throw IOException("Unable to save plant progress")
    }
}
