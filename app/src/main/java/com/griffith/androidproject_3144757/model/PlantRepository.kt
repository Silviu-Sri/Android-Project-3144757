package com.griffith.androidproject_3144757.model

import android.content.Context
import com.griffith.androidproject_3144757.pages.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.io.IOException

class PlantRepository(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences("plant", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    suspend fun update(weather: Weather?, now: Long = System.currentTimeMillis()): PlantState =
        withContext(Dispatchers.IO) {
            mutex.withLock {
                val saved = load(now)
                val updated = PlantCare.advance(saved, weather, now)
                save(updated)
                updated
            }
        }

    suspend fun read(): PlantState = withContext(Dispatchers.IO) {
        mutex.withLock { load(System.currentTimeMillis()) }
    }

    suspend fun updateSuns(suns: Int): PlantState = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = load(System.currentTimeMillis())
            val updated = current.copy(suns = suns)
            save(updated)
            updated
        }
    }

    suspend fun buyItem(itemId: String, cost: Int): PlantState = withContext(Dispatchers.IO) {
        require(cost >= 0 && itemId.isNotBlank())
        mutex.withLock {
            val current = load(System.currentTimeMillis())
            val count = current.inventory[itemId] ?: 0
            if (current.suns < cost || count == Int.MAX_VALUE) return@withLock current
            val updated = current.copy(
                suns = current.suns - cost,
                inventory = current.inventory + (itemId to count + 1)
            )
            save(updated)
            updated
        }
    }

    private fun load(now: Long): PlantState {
        if (!preferences.contains("updated")) return PlantState(lastUpdatedEpochMillis = now)
        return try {
            PlantState(
                preferences.getFloat("moisture", 60f),
                preferences.getFloat("health", 100f),
                preferences.getFloat("growth", 0f),
                preferences.getLong("updated", now),
                preferences.getFloat("happiness", 80f),
                preferences.getInt("suns", 50),
                preferences.all.filterKeys { it.startsWith("inventory_") }
                    .mapNotNull { (key, value) ->
                        (value as? Int)?.takeIf { it >= 0 }?.let { key.removePrefix("inventory_") to it }
                    }.toMap()
            )
        } catch (_: IllegalArgumentException) {
            PlantState(lastUpdatedEpochMillis = now)
        } catch (_: ClassCastException) {
            PlantState(lastUpdatedEpochMillis = now)
        }
    }

    private fun save(state: PlantState) {
        val editor = preferences.edit()
        state.inventory.forEach { (itemId, count) -> editor.putInt("inventory_$itemId", count) }
        val saved = editor
            .putFloat("moisture", state.soilMoisturePercent)
            .putFloat("health", state.healthPercent)
            .putFloat("growth", state.growthProgressPercent)
            .putFloat("happiness", state.happinessPercent)
            .putInt("suns", state.suns)
            .putLong("updated", state.lastUpdatedEpochMillis)
            .commit()
        if (!saved) throw IOException("Unable to save plant progress")
    }
}
