package com.griffith.androidproject_3144757.model

data class PlantState(
    val soilMoisturePercent: Float = 60f,
    val healthPercent: Float = 100f,
    val growthProgressPercent: Float = 0f,
    val lastUpdatedEpochMillis: Long = System.currentTimeMillis()
) {
    init {
        require(soilMoisturePercent in 0f..100f) {
            "Soil moisture must be between 0 and 100 percent"
        }
        require(healthPercent in 0f..100f) {
            "Health must be between 0 and 100 percent"
        }
        require(growthProgressPercent in 0f..100f) {
            "Growth progress must be between 0 and 100 percent"
        }
        require(lastUpdatedEpochMillis >= 0L) {
            "Last update time must be a nonnegative epoch timestamp in milliseconds"
        }
    }
}
