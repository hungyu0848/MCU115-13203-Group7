package com.example.wheretogo

import java.util.TimeZone

/** A duration must retain its origin; an estimate or default is not an observation. */
sealed class DurationOrigin {
    object UserInput : DurationOrigin()
    data class ExternalEstimate(val provider: String) : DurationOrigin() {
        init { require(provider.isNotBlank()) { "Estimate provider is required" } }
    }
    data class SystemDefault(val description: String) : DurationOrigin() {
        init { require(description.isNotBlank()) { "Default description is required" } }
    }
}

data class DurationInput(val minutes: Long, val origin: DurationOrigin) {
    init { require(minutes >= 0) { "Duration must not be negative" } }
}

/** The epoch identifies a moment; the explicitly selected zone is needed for display. */
data class UserDepartureTime(val epochSeconds: Long, val timeZoneId: String) {
    init {
        require(timeZoneId.isNotBlank() && TimeZone.getAvailableIDs().contains(timeZoneId)) {
            "A recognized time zone is required"
        }
    }
}

/** travel is from the previous location; visit is needed before the next stop. */
data class StopTimingInput(val stopId: String, val travel: DurationInput?, val visit: DurationInput?) {
    init { require(stopId.isNotBlank()) { "Stop ID is required" } }
}

enum class MissingTimingField { DEPARTURE, STOPS, TRAVEL, VISIT }
data class MissingTimingInput(val field: MissingTimingField, val stopId: String? = null)

sealed class TimingInputResult {
    data class Incomplete(val missing: List<MissingTimingInput>) : TimingInputResult()
    data class OutOfRange(val reason: String) : TimingInputResult()
    /** A calculation, not a verified road ETA; sources must remain visible to its caller. */
    data class Candidate(val departure: UserDepartureTime, val stops: List<StopTimingInput>,
                         val arrivalsEpochSeconds: List<Long>) : TimingInputResult()
}

object ItineraryTimingInput {
    fun calculate(departure: UserDepartureTime?, stops: List<StopTimingInput>): TimingInputResult {
        require(stops.map { it.stopId }.distinct().size == stops.size) { "Duplicate stop IDs" }
        val missing = mutableListOf<MissingTimingInput>()
        if (departure == null) missing.add(MissingTimingInput(MissingTimingField.DEPARTURE))
        if (stops.isEmpty()) missing.add(MissingTimingInput(MissingTimingField.STOPS))
        stops.forEachIndexed { index, stop ->
            if (stop.travel == null) missing.add(MissingTimingInput(MissingTimingField.TRAVEL, stop.stopId))
            if (index < stops.lastIndex && stop.visit == null)
                missing.add(MissingTimingInput(MissingTimingField.VISIT, stop.stopId))
        }
        if (missing.isNotEmpty()) return TimingInputResult.Incomplete(missing.toList())

        val selectedDeparture = requireNotNull(departure)
        val snapshot = stops.toList()
        return try {
            // The final visit is not needed to determine its arrival; never invent it.
            val preceding = snapshot.dropLast(1)
            val legs = preceding.map { StopTiming(requireNotNull(it.travel).minutes, requireNotNull(it.visit).minutes) }
            val earlierArrivals = ItineraryTiming.arrivalTimes(selectedDeparture.epochSeconds, legs)
            val lastDeparture = if (preceding.isEmpty()) selectedDeparture.epochSeconds else {
                Math.addExact(earlierArrivals.last(), Math.multiplyExact(requireNotNull(preceding.last().visit).minutes, 60L))
            }
            val lastArrival = Math.addExact(lastDeparture, Math.multiplyExact(requireNotNull(snapshot.last().travel).minutes, 60L))
            val arrivals = earlierArrivals + lastArrival
            TimingInputResult.Candidate(selectedDeparture, snapshot, arrivals)
        } catch (_: ArithmeticException) {
            TimingInputResult.OutOfRange("Timing arithmetic exceeds supported range")
        }
    }
}
