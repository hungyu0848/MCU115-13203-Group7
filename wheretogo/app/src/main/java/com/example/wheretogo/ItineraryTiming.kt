package com.example.wheretogo

/** Explicit per-stop estimates; no departure time or transport mode is assumed. */
data class StopTiming(val travelMinutes: Long, val visitMinutes: Long) {
    init {
        require(travelMinutes >= 0 && visitMinutes >= 0) { "Durations must not be negative" }
    }
}

object ItineraryTiming {
    /** UTC epoch seconds at each stop, including the preceding stops' visit times. */
    fun arrivalTimes(departureEpochSeconds: Long, stops: List<StopTiming>): List<Long> {
        var cursor = departureEpochSeconds
        return stops.mapIndexed { index, stop ->
            val arrival = Math.addExact(cursor, Math.multiplyExact(stop.travelMinutes, 60L))
            if (index < stops.lastIndex) {
                cursor = Math.addExact(arrival, Math.multiplyExact(stop.visitMinutes, 60L))
            }
            arrival
        }
    }
}
