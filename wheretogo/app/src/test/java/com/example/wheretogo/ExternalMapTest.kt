package com.example.wheretogo

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class ExternalMapTest {
    @Test fun coordinatesAreStableAcrossDeviceLocales() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.FRANCE)
            assertEquals("geo:0,0?q=25.25147,121.46991",
                ExternalMap.destination(Departure("淺水灣", 25.25147, 121.46991)))
            assertEquals("geo:0,0?q=-33.5,-70.25",
                ExternalMap.destination(Departure("南半球", -33.5, -70.25)))
        } finally {
            Locale.setDefault(previous)
        }
    }
}
