package com.example.wheretogo

/** Open a single stop in a compatible external map; navigation is chosen there. */
object ExternalMap {
    fun destination(location: Departure): String =
        "geo:0,0?q=${location.latitude},${location.longitude}"
}
