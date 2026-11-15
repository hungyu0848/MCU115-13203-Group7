package com.example.wheretogo

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class SavedTripCodecTest {
    private fun sample(): SavedTripSnapshot {
        val conditions = TripConditions(Departure("淡水", 25.1, 121.4), AvailableTime.HALF_DAY, 500,
            setOf(PlacePreference.NATURE, PlacePreference.FOOD))
        val stops = listOf(
            LocalAttraction("b", Departure("白沙灣", 25.2, 121.5), "地址 B", setOf(PlacePreference.OUTDOORS), 60, 0, "https://example.org/b"),
            LocalAttraction("a", Departure("淺水灣", 25.3, 121.6), "地址 A", emptySet(), 30, 50, "https://example.org/a")
        )
        return SavedTripSnapshot(conditions, stops, SavedRouteEstimate(25.25, 70, 90, 50, "直線距離粗估"))
    }

    @Test fun roundTripRetainsOrderedSnapshotsAndEstimateProvenance() {
        val original = sample()
        val restored = SavedTripCodec.decode(SavedTripCodec.encode(original))
        assertEquals(original.conditions, restored.conditions)
        assertEquals(original.stops, restored.stops)
        assertEquals(original.estimate, restored.estimate)
        assertEquals(listOf("b", "a"), restored.stops.map { it.id })
    }

    @Test fun rejectsUnknownVersionsAndMalformedOrInconsistentSnapshots() {
        val valid = SavedTripCodec.encode(sample())
        assertThrows(IOException::class.java) { SavedTripCodec.decode("{}") }
        assertThrows(IOException::class.java) { SavedTripCodec.decode(valid.replace("\"version\":1", "\"version\":2")) }
        assertThrows(IOException::class.java) { SavedTripCodec.decode(valid.replace("\"version\":1", "\"version\":1,\"unexpected\":true")) }
        assertThrows(IOException::class.java) { SavedTripCodec.decode(valid.replace("\"budgetTwd\":500", "\"budgetTwd\":1.5")) }
        assertThrows(IOException::class.java) { SavedTripCodec.decode(valid.replace("\"HALF_DAY\"", "\"UNKNOWN\"")) }
        assertThrows(IOException::class.java) { SavedTripCodec.decode(valid.replace("\"id\":\"b\"", "\"id\":\"a\"")) }
        assertThrows(IOException::class.java) { SavedTripCodec.decode(valid.replace("\"latitude\":25.1", "\"latitude\":91")) }
    }
}
