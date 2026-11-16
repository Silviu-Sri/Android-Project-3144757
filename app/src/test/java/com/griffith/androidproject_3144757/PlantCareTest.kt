package com.griffith.androidproject_3144757

import com.griffith.androidproject_3144757.model.PlantCare
import com.griffith.androidproject_3144757.model.PlantState
import org.junit.Assert.*
import org.junit.Test

class PlantCareTest {
    @Test fun longAbsenceIsCappedAndGrowthNeverReverses() {
        val plant = PlantState(soilMoisturePercent = 0f, growthProgressPercent = 40f, lastUpdatedEpochMillis = 0)
        val day = PlantCare.advance(plant, null, 86_400_000L)
        val week = PlantCare.advance(plant, null, 604_800_000L)
        assertEquals(day.healthPercent, week.healthPercent, 0.001f)
        assertEquals(40f, week.growthProgressPercent, 0.001f)
        assertTrue(week.healthPercent >= 20f)
        assertEquals(604_800_000L, week.lastUpdatedEpochMillis)
    }

    @Test fun repeatedUpdatesDoNotApplyTimeTwice() {
        val plant = PlantState(lastUpdatedEpochMillis = 1000L)
        val updated = PlantCare.advance(plant, null, 3_601_000L)
        assertEquals(updated, PlantCare.advance(updated, null, 3_601_000L))
        assertEquals(updated, PlantCare.advance(updated, null, 1000L))
        assertTrue(updated.growthProgressPercent > 0f)
    }

    @Test fun wateringIsBounded() {
        val plant = PlantState(soilMoisturePercent = 95f)
        assertEquals(100f, PlantCare.water(plant).soilMoisturePercent, 0f)
    }
}
