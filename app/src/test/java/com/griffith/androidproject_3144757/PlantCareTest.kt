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

    @Test fun happinessRespondsToCareAndRecovers() {
        val comfortable = PlantState(soilMoisturePercent = 60f, happinessPercent = 50f, lastUpdatedEpochMillis = 0L)
        val neglected = comfortable.copy(soilMoisturePercent = 0f)
        val afterGoodCare = PlantCare.advance(comfortable, null, 3_600_000L)
        val afterNeglect = PlantCare.advance(neglected, null, 3_600_000L)
        assertTrue(afterGoodCare.happinessPercent > comfortable.happinessPercent)
        assertTrue(afterNeglect.happinessPercent < neglected.happinessPercent)
        val recovered = PlantCare.advance(afterNeglect.copy(soilMoisturePercent = 60f), null, 7_200_000L)
        assertTrue(recovered.happinessPercent > afterNeglect.happinessPercent)
        assertTrue(PlantCare.advance(neglected.copy(happinessPercent = 20f), null, 86_400_000L).happinessPercent >= 20f)
    }

    @Test fun wateringIsBounded() {
        val plant = PlantState(soilMoisturePercent = 95f)
        assertEquals(100f, PlantCare.water(plant).soilMoisturePercent, 0f)
    }
}
