package com.example.wheretogo

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ItineraryTimingTest {
    @Test fun arrivalIncludesEarlierTravelAndVisitsButNotCurrentVisit() {
        val stops = listOf(StopTiming(20, 60), StopTiming(15, 30), StopTiming(5, 0))
        assertEquals(listOf(2_200L, 6_700L, 8_800L), ItineraryTiming.arrivalTimes(1_000L, stops))
    }

    @Test fun emptyAndZeroDurationsDoNotInventTimes() {
        assertEquals(emptyList<Long>(), ItineraryTiming.arrivalTimes(42L, emptyList()))
        assertEquals(listOf(42L), ItineraryTiming.arrivalTimes(42L, listOf(StopTiming(0, 0))))
        assertEquals(listOf(Long.MAX_VALUE),
            ItineraryTiming.arrivalTimes(Long.MAX_VALUE, listOf(StopTiming(0, Long.MAX_VALUE))))
    }

    @Test fun rejectsNegativeAndOverflowDurations() {
        assertThrows(IllegalArgumentException::class.java) { StopTiming(-1, 0) }
        assertThrows(IllegalArgumentException::class.java) { StopTiming(0, -1) }
        assertThrows(ArithmeticException::class.java) {
            ItineraryTiming.arrivalTimes(Long.MAX_VALUE, listOf(StopTiming(1, 0)))
        }
        assertThrows(ArithmeticException::class.java) {
            ItineraryTiming.arrivalTimes(0, listOf(StopTiming(Long.MAX_VALUE, 0)))
        }
    }
}
