package com.example.wheretogo

import kotlin.math.*

data class LocalAttraction(
    val id: String,
    val location: Departure,
    val address: String,
    val preferences: Set<PlacePreference>,
    val visitMinutes: Int,
    val admissionTwd: Int,
    val sourceUrl: String
)

/** Small, curated starting catalogue; not an online search result or exhaustive POI list. */
object PlanningCatalog {
    val attractions = listOf(
        LocalAttraction("qianshui", Departure("淺水灣海濱公園", 25.25147, 121.46991),
            "新北市三芝區後厝里北勢子淡金公路",
            setOf(PlacePreference.NATURE, PlacePreference.PHOTOGRAPHY, PlacePreference.OUTDOORS),
            60, 0, "https://newtaipei.travel/zh-tw/attractions/detail/109631"),
        LocalAttraction("baisha", Departure("白沙灣自然中心", 25.27841, 121.521283),
            "新北市石門區德茂里下員坑33-6號",
            setOf(PlacePreference.NATURE, PlacePreference.OUTDOORS),
            60, 0, "https://newtaipei.travel/zh-tw/attractions/detail/403570"),
        LocalAttraction("fugui", Departure("富貴角", 25.29828, 121.53763),
            "新北市石門區富貴角",
            setOf(PlacePreference.NATURE, PlacePreference.PHOTOGRAPHY, PlacePreference.OUTDOORS),
            60, 0, "https://newtaipei.travel/zh-tw/attractions/detail/109642")
    )

    val departures = listOf(Departure("淡水捷運站", 25.167583, 121.446000)) + attractions.map { it.location }

    fun distanceKm(a: Departure, b: Departure): Double {
        val lat = Math.toRadians(b.latitude - a.latitude)
        val lon = Math.toRadians(b.longitude - a.longitude)
        val h = sin(lat / 2).pow(2) + cos(Math.toRadians(a.latitude)) *
            cos(Math.toRadians(b.latitude)) * sin(lon / 2).pow(2)
        return 6371.0 * 2 * asin(sqrt(h.coerceIn(0.0, 1.0)))
    }

    // A transparent screening estimate, not a road routing engine or travel-time guarantee.
    fun estimatedRoundTripMinutes(origin: Departure, spot: LocalAttraction): Int =
        ceil(distanceKm(origin, spot.location) * 2 * 1.5 / 25 * 60).toInt() + spot.visitMinutes

    fun recommend(conditions: TripConditions, query: String = ""): List<LocalAttraction> =
        attractions.filter { spot ->
            distanceKm(conditions.departure, spot.location) > 0.05 &&
                spot.admissionTwd <= conditions.budgetTwd &&
                estimatedRoundTripMinutes(conditions.departure, spot) <= conditions.availableTime.minutes &&
                (conditions.preferences.isEmpty() || spot.preferences.any { it in conditions.preferences }) &&
                (query.isBlank() || spot.location.name.contains(query.trim(), true) || spot.address.contains(query.trim(), true))
        }.sortedBy { distanceKm(conditions.departure, it.location) }
}
