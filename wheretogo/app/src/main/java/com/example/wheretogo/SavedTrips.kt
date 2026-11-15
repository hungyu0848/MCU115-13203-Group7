package com.example.wheretogo

import java.util.Collections

/** A stored estimate must retain its stated method; it is not road navigation. */
data class SavedRouteEstimate(
    val straightDistanceKm: Double,
    val travelMinutes: Long,
    val visitMinutes: Long,
    val admissionTwd: Long,
    val methodDescription: String
) {
    init {
        require(straightDistanceKm.isFinite() && straightDistanceKm >= 0)
        require(travelMinutes >= 0 && visitMinutes >= 0 && admissionTwd >= 0)
        require(methodDescription.isNotBlank())
        Math.addExact(travelMinutes, visitMinutes)
    }
}

/** A snapshot keeps the selected order and details even if the catalogue changes later. */
class SavedTripSnapshot(conditions: TripConditions, stops: List<LocalAttraction>, val estimate: SavedRouteEstimate) {
    val conditions: TripConditions
    val stops: List<LocalAttraction>

    init {
        require(stops.isNotEmpty()) { "At least one stop is required" }
        require(stops.all { it.id.isNotBlank() && it.visitMinutes >= 0 && it.admissionTwd >= 0 })
        require(stops.map { it.id }.distinct().size == stops.size) { "Duplicate stop IDs" }
        this.conditions = conditions.copy(preferences = Collections.unmodifiableSet(conditions.preferences.toSet()))
        this.stops = Collections.unmodifiableList(stops.map {
            it.copy(preferences = Collections.unmodifiableSet(it.preferences.toSet()))
        })
    }
}

data class SavedTripRecord(val id: Long, val accountId: Long, val snapshot: SavedTripSnapshot) {
    init { require(id > 0 && accountId > 0) }
}

/** Implementations must atomically scope every query and deletion to accountId. */
interface SavedTripStorage {
    fun insert(accountId: Long, snapshot: SavedTripSnapshot): Long
    fun find(accountId: Long, tripId: Long): SavedTripRecord?
    fun list(accountId: Long): List<SavedTripRecord>
    fun delete(accountId: Long, tripId: Long): Boolean
}

/** The caller must supply the authenticated account; this class does not authenticate. */
class SavedTrips(private val storage: SavedTripStorage) {
    fun save(accountId: Long, snapshot: SavedTripSnapshot): Long {
        require(accountId > 0)
        return storage.insert(accountId, snapshot).also { check(it > 0) { "Storage returned invalid ID" } }
    }

    fun read(accountId: Long, tripId: Long): SavedTripRecord? {
        require(accountId > 0 && tripId > 0)
        return storage.find(accountId, tripId)?.also {
            check(it.accountId == accountId && it.id == tripId) { "Storage returned another trip" }
        }
    }

    fun list(accountId: Long): List<SavedTripRecord> {
        require(accountId > 0)
        val records = storage.list(accountId).toList()
        check(records.all { it.accountId == accountId }) { "Storage returned another account's trip" }
        return records
    }

    fun delete(accountId: Long, tripId: Long): Boolean {
        require(accountId > 0 && tripId > 0)
        return storage.delete(accountId, tripId)
    }
}
