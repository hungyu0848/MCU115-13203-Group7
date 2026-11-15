package com.example.wheretogo

import org.junit.Assert.*
import org.junit.Test

class SavedTripsTest {
    private class MemoryStorage : SavedTripStorage {
        private var nextId = 1L
        private val records = mutableMapOf<Pair<Long, Long>, SavedTripRecord>()
        override fun insert(accountId: Long, snapshot: SavedTripSnapshot): Long {
            val id = nextId++
            records[accountId to id] = SavedTripRecord(id, accountId, snapshot)
            return id
        }
        override fun find(accountId: Long, tripId: Long) = records[accountId to tripId]
        override fun list(accountId: Long) = records.values.filter { it.accountId == accountId }
        override fun delete(accountId: Long, tripId: Long) = records.remove(accountId to tripId) != null
    }

    private val origin = Departure("起點", 25.0, 121.0)
    private val estimate = SavedRouteEstimate(20.0, 48, 120, 0, "直線距離估算，含回程")
    private fun spot(id: String, preferences: Set<PlacePreference> = emptySet()) =
        LocalAttraction(id, Departure(id, 25.1, 121.1), "地址", preferences, 60, 0, "https://example.org")
    private fun snapshot() = SavedTripSnapshot(TripConditions(origin, AvailableTime.HALF_DAY, 500),
        listOf(spot("a"), spot("b")), estimate)

    @Test fun saveReadListAndDeleteAreScopedToTheOwner() {
        val trips = SavedTrips(MemoryStorage())
        val first = trips.save(1, snapshot())
        val second = trips.save(2, snapshot())
        assertEquals(listOf("a", "b"), trips.read(1, first)!!.snapshot.stops.map { it.id })
        assertNull(trips.read(2, first))
        assertEquals(listOf(second), trips.list(2).map { it.id })
        assertFalse(trips.delete(2, first))
        assertNotNull(trips.read(1, first))
        assertTrue(trips.delete(1, first))
        assertNull(trips.read(1, first))
        assertNotNull(trips.read(2, second))
    }

    @Test fun snapshotDoesNotFollowMutableInputOrCatalogueChanges() {
        val preferences = mutableSetOf(PlacePreference.NATURE)
        val spotPreferences = mutableSetOf(PlacePreference.OUTDOORS)
        val stops = mutableListOf(spot("a", spotPreferences), spot("b"))
        val snapshot = SavedTripSnapshot(TripConditions(origin, AvailableTime.HALF_DAY, 500, preferences), stops, estimate)
        preferences.clear()
        spotPreferences.clear()
        stops.reverse()
        stops.clear()
        assertEquals(setOf(PlacePreference.NATURE), snapshot.conditions.preferences)
        assertEquals(setOf(PlacePreference.OUTDOORS), snapshot.stops[0].preferences)
        assertEquals(listOf("a", "b"), snapshot.stops.map { it.id })
        assertEquals("直線距離估算，含回程", snapshot.estimate.methodDescription)
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.stops as MutableList).clear() }
        assertThrows(UnsupportedOperationException::class.java) { (snapshot.conditions.preferences as MutableSet).clear() }
    }

    @Test fun invalidIdentifiersAndSnapshotsFailBeforeStorageAccess() {
        val trips = SavedTrips(MemoryStorage())
        assertThrows(IllegalArgumentException::class.java) { trips.save(0, snapshot()) }
        assertThrows(IllegalArgumentException::class.java) { trips.read(1, 0) }
        assertThrows(IllegalArgumentException::class.java) { trips.delete(-1, 1) }
        assertThrows(IllegalArgumentException::class.java) { SavedTripSnapshot(TripConditions(origin, AvailableTime.ONE_DAY, 0), emptyList(), estimate) }
        assertThrows(IllegalArgumentException::class.java) { SavedTripSnapshot(TripConditions(origin, AvailableTime.ONE_DAY, 0), listOf(spot("a"), spot("a")), estimate) }
        assertThrows(IllegalArgumentException::class.java) { SavedRouteEstimate(Double.NaN, 0, 0, 0, "估算") }
        assertThrows(IllegalArgumentException::class.java) { SavedRouteEstimate(0.0, 0, 0, 0, " ") }
        assertThrows(ArithmeticException::class.java) { SavedRouteEstimate(0.0, Long.MAX_VALUE, 1, 0, "估算") }
    }

    @Test fun rejectsStorageResultsFromAnotherAccount() {
        val foreign = SavedTripRecord(5, 2, snapshot())
        val brokenStorage = object : SavedTripStorage {
            override fun insert(accountId: Long, snapshot: SavedTripSnapshot) = 5L
            override fun find(accountId: Long, tripId: Long) = foreign
            override fun list(accountId: Long) = listOf(foreign)
            override fun delete(accountId: Long, tripId: Long) = false
        }
        val trips = SavedTrips(brokenStorage)
        assertThrows(IllegalStateException::class.java) { trips.read(1, 5) }
        assertThrows(IllegalStateException::class.java) { trips.list(1) }
    }
}
