package com.example.wheretogo

import org.junit.Assert.*
import org.junit.Test

class TripConditionsTest {
    @Test fun approvedHalfDayExampleKeepsSelectedPreferencesAndBudget() {
        val conditions = TripConditions(
            Departure("淡水捷運站", 25.1678, 121.4456), AvailableTime.HALF_DAY, 500,
            setOf(PlacePreference.NATURE, PlacePreference.PHOTOGRAPHY)
        )
        assertEquals(240, conditions.availableTime.minutes)
        assertEquals(500, conditions.budgetTwd)
        assertEquals(2, conditions.preferences.size)
        assertTrue(conditions.preferences.contains(PlacePreference.NATURE))
        assertTrue(conditions.preferences.contains(PlacePreference.PHOTOGRAPHY))
    }

    @Test fun freeOutingAndNoPreferenceAreValid() {
        val conditions = TripConditions(Departure("出發地", 0.0, 0.0), AvailableTime.TWO_HOURS, 0)
        assertTrue(conditions.preferences.isEmpty())
        assertEquals(120, conditions.availableTime.minutes)
        assertEquals(480, AvailableTime.ONE_DAY.minutes)
    }

    @Test fun customBudgetRejectsInvalidAndOverflowingAmountsWithoutDefaulting() {
        assertEquals(300, ConditionsInput.parseBudgetTwd("300"))
        assertEquals(500, ConditionsInput.parseBudgetTwd(" 500 "))
        assertEquals(1000, ConditionsInput.parseBudgetTwd("1000"))
        assertEquals(0, ConditionsInput.parseBudgetTwd("0"))
        listOf("", " ", "-1", "1.5", "1,000", "NT500", "５００", "2147483648").forEach {
            assertNull("Invalid budget: $it", ConditionsInput.parseBudgetTwd(it))
        }
    }

    @Test fun unresolvedOrInvalidDepartureCannotReachRecommendations() {
        listOf("", "  ").forEach { name ->
            assertThrows(IllegalArgumentException::class.java) { Departure(name, 25.0, 121.0) }
        }
        listOf(Double.NaN, Double.POSITIVE_INFINITY, -90.001, 90.001).forEach { latitude ->
            assertThrows(IllegalArgumentException::class.java) { Departure("出發地", latitude, 121.0) }
        }
        listOf(Double.NaN, Double.NEGATIVE_INFINITY, -180.001, 180.001).forEach { longitude ->
            assertThrows(IllegalArgumentException::class.java) { Departure("出發地", 25.0, longitude) }
        }
        assertEquals(-90.0, Departure("南極", -90.0, -180.0).latitude, 0.0)
        assertEquals(180.0, Departure("邊界", 90.0, 180.0).longitude, 0.0)
    }

    @Test fun negativeBudgetCannotEnterThePlanningModel() {
        assertThrows(IllegalArgumentException::class.java) {
            TripConditions(Departure("出發地", 25.0, 121.0), AvailableTime.ONE_DAY, -1)
        }
    }
}
