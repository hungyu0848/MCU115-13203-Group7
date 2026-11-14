package com.example.wheretogo

/** The time limit includes travel, as specified by the conditions screen. */
enum class AvailableTime(val minutes: Int) {
    TWO_HOURS(120), HALF_DAY(240), ONE_DAY(480)
}

enum class PlacePreference {
    NATURE, HISTORY, FOOD, PHOTOGRAPHY, OUTDOORS
}

/** Resolve the departure location before distance-based recommendations run. */
data class Departure(val name: String, val latitude: Double, val longitude: Double) {
    init {
        require(name.isNotBlank()) { "Departure name is required" }
        require(latitude.isFinite() && latitude in -90.0..90.0) { "Invalid latitude" }
        require(longitude.isFinite() && longitude in -180.0..180.0) { "Invalid longitude" }
    }
}

data class TripConditions(
    val departure: Departure,
    val availableTime: AvailableTime,
    val budgetTwd: Int,
    // Empty preferences mean mixed recommendations, matching the approved UI.
    val preferences: Set<PlacePreference> = emptySet()
) {
    init {
        require(budgetTwd >= 0) { "Budget cannot be negative" }
    }
}

object ConditionsInput {
    /** Whole NT dollars; zero permits a free outing. Never silently default invalid input. */
    fun parseBudgetTwd(raw: String): Int? {
        val text = raw.trim()
        if (text.isEmpty() || text.any { it !in '0'..'9' }) return null
        return text.toIntOrNull()
    }
}
