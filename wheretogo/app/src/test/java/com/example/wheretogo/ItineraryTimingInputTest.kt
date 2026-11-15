package com.example.wheretogo

import org.junit.Assert.*
import org.junit.Test

class ItineraryTimingInputTest {
    private val departure = UserDepartureTime(1_000, "Asia/Taipei")
    private val user = DurationInput(20, DurationOrigin.UserInput)
    private val estimate = DurationInput(15, DurationOrigin.ExternalEstimate("provider"))
    private val default = DurationInput(60, DurationOrigin.SystemDefault("catalogue"))

    @Test fun missingInputsAreReportedWithoutInventingValues() {
        val result = ItineraryTimingInput.calculate(null,
            listOf(StopTimingInput("a", null, null), StopTimingInput("b", user, null)))
            as TimingInputResult.Incomplete
        assertEquals(listOf(MissingTimingInput(MissingTimingField.DEPARTURE),
            MissingTimingInput(MissingTimingField.TRAVEL, "a"),
            MissingTimingInput(MissingTimingField.VISIT, "a")), result.missing)
        assertEquals(listOf(MissingTimingInput(MissingTimingField.STOPS)),
            (ItineraryTimingInput.calculate(departure, emptyList()) as TimingInputResult.Incomplete).missing)
    }

    @Test fun candidateUsesExistingCalculationAndRetainsEverySource() {
        val stops = mutableListOf(StopTimingInput("a", user, default), StopTimingInput("b", estimate, null))
        val result = ItineraryTimingInput.calculate(departure, stops) as TimingInputResult.Candidate
        stops.clear()
        assertEquals(listOf(2_200L, 6_700L), result.arrivalsEpochSeconds)
        assertEquals(DurationOrigin.UserInput, result.stops[0].travel?.origin)
        assertEquals(DurationOrigin.SystemDefault("catalogue"), result.stops[0].visit?.origin)
        assertEquals(DurationOrigin.ExternalEstimate("provider"), result.stops[1].travel?.origin)
        assertNull(result.stops[1].visit)
        assertEquals("Asia/Taipei", result.departure.timeZoneId)
    }

    @Test fun invalidInputsAndArithmeticOverflowAreExplicit() {
        assertThrows(IllegalArgumentException::class.java) { DurationInput(-1, DurationOrigin.UserInput) }
        assertThrows(IllegalArgumentException::class.java) { DurationOrigin.ExternalEstimate(" ") }
        assertThrows(IllegalArgumentException::class.java) { DurationOrigin.SystemDefault("") }
        assertThrows(IllegalArgumentException::class.java) { UserDepartureTime(0, "unknown/zone") }
        assertThrows(IllegalArgumentException::class.java) { StopTimingInput(" ", user, null) }
        assertThrows(IllegalArgumentException::class.java) {
            ItineraryTimingInput.calculate(departure, listOf(StopTimingInput("a", user, null), StopTimingInput("a", user, null)))
        }
        val result = ItineraryTimingInput.calculate(UserDepartureTime(Long.MAX_VALUE, "UTC"),
            listOf(StopTimingInput("a", user, null)))
        assertTrue(result is TimingInputResult.OutOfRange)
        val tooLong = DurationInput(Long.MAX_VALUE, DurationOrigin.UserInput)
        assertTrue(ItineraryTimingInput.calculate(departure, listOf(
            StopTimingInput("a", user, tooLong), StopTimingInput("b", user, null)
        )) is TimingInputResult.OutOfRange)
    }
}
