package com.example.wheretogo

import org.junit.Assert.*
import org.junit.Test

class PlanningCatalogTest {
    private val start = PlanningCatalog.departures.first()

    @Test fun halfDayNorthCoastMatchesApprovedExampleWithoutNetwork() {
        val conditions = TripConditions(start, AvailableTime.HALF_DAY, 500,
            setOf(PlacePreference.NATURE, PlacePreference.PHOTOGRAPHY))
        val results = PlanningCatalog.recommend(conditions)
        assertEquals(listOf("qianshui", "baisha", "fugui"), results.map { it.id })
        assertTrue(results.all { it.sourceUrl.startsWith("https://newtaipei.travel/") })
    }

    @Test fun timeLimitActuallyExcludesDistantStopsAndNoPreferenceAllowsMixedResults() {
        val results = PlanningCatalog.recommend(TripConditions(start, AvailableTime.TWO_HOURS, 0))
        // Even the nearest 60-minute stop exceeds 120 minutes with this round-trip estimate.
        assertTrue(results.isEmpty())
    }

    @Test fun unmatchedPreferenceAndUnknownQueryReturnEmptyRatherThanFakeResults() {
        assertTrue(PlanningCatalog.recommend(TripConditions(start, AvailableTime.HALF_DAY, 500,
            setOf(PlacePreference.FOOD))).isEmpty())
        assertTrue(PlanningCatalog.recommend(TripConditions(start, AvailableTime.HALF_DAY, 500), "不存在").isEmpty())
        assertEquals(listOf("baisha"), PlanningCatalog.recommend(
            TripConditions(start, AvailableTime.HALF_DAY, 500), " 白沙灣 ").map { it.id })
    }

    @Test fun originIsNotRecommendedAsAStopAndDistanceIsSymmetric() {
        val origin = PlanningCatalog.attractions.first().location
        assertFalse(PlanningCatalog.recommend(TripConditions(origin, AvailableTime.ONE_DAY, 500))
            .any { it.id == "qianshui" })
        assertEquals(0.0, PlanningCatalog.distanceKm(origin, origin), 0.000001)
        assertEquals(PlanningCatalog.distanceKm(start, origin), PlanningCatalog.distanceKm(origin, start), 0.000001)
        assertTrue(PlanningCatalog.distanceKm(start, origin) in 9.0..11.0)
    }
}
