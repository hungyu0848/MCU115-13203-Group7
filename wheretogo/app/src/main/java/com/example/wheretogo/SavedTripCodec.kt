package com.example.wheretogo

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.IOException

/** Versioned snapshot payload. Account ownership belongs in the database, not this JSON. */
object SavedTripCodec {
    private const val VERSION = 1

    fun encode(snapshot: SavedTripSnapshot): String {
        val root = JsonObject()
        root.addProperty("version", VERSION)
        root.add("conditions", JsonObject().apply {
            add("departure", location(snapshot.conditions.departure))
            addProperty("availableTime", snapshot.conditions.availableTime.name)
            addProperty("budgetTwd", snapshot.conditions.budgetTwd)
            add("preferences", preferences(snapshot.conditions.preferences))
        })
        root.add("stops", JsonArray().apply {
            snapshot.stops.forEach { stop -> add(JsonObject().apply {
                addProperty("id", stop.id)
                add("location", location(stop.location))
                addProperty("address", stop.address)
                add("preferences", preferences(stop.preferences))
                addProperty("visitMinutes", stop.visitMinutes)
                addProperty("admissionTwd", stop.admissionTwd)
                addProperty("sourceUrl", stop.sourceUrl)
            }) }
        })
        root.add("estimate", JsonObject().apply {
            addProperty("straightDistanceKm", snapshot.estimate.straightDistanceKm)
            addProperty("travelMinutes", snapshot.estimate.travelMinutes)
            addProperty("visitMinutes", snapshot.estimate.visitMinutes)
            addProperty("admissionTwd", snapshot.estimate.admissionTwd)
            addProperty("methodDescription", snapshot.estimate.methodDescription)
        })
        return root.toString()
    }

    fun decode(json: String): SavedTripSnapshot = try {
        val root = JsonParser.parseString(json).asJsonObject
        requireKeys(root, "version", "conditions", "stops", "estimate")
        require(number(root, "version").intValueExact() == VERSION) { "Unsupported snapshot version" }
        val conditions = root.getAsJsonObject("conditions")
        requireKeys(conditions, "departure", "availableTime", "budgetTwd", "preferences")
        val tripConditions = TripConditions(
            readLocation(conditions.getAsJsonObject("departure")),
            AvailableTime.valueOf(string(conditions, "availableTime")),
            number(conditions, "budgetTwd").intValueExact(),
            readPreferences(conditions.getAsJsonArray("preferences"))
        )
        val stops = root.getAsJsonArray("stops").map { element ->
            val stop = element.asJsonObject
            requireKeys(stop, "id", "location", "address", "preferences", "visitMinutes", "admissionTwd", "sourceUrl")
            LocalAttraction(string(stop, "id"), readLocation(stop.getAsJsonObject("location")),
                string(stop, "address"), readPreferences(stop.getAsJsonArray("preferences")),
                number(stop, "visitMinutes").intValueExact(), number(stop, "admissionTwd").intValueExact(),
                string(stop, "sourceUrl"))
        }
        val estimate = root.getAsJsonObject("estimate")
        requireKeys(estimate, "straightDistanceKm", "travelMinutes", "visitMinutes", "admissionTwd", "methodDescription")
        SavedTripSnapshot(tripConditions, stops, SavedRouteEstimate(
            number(estimate, "straightDistanceKm").toDouble(), number(estimate, "travelMinutes").longValueExact(),
            number(estimate, "visitMinutes").longValueExact(), number(estimate, "admissionTwd").longValueExact(),
            string(estimate, "methodDescription")))
    } catch (e: Exception) {
        throw IOException("Invalid or unsupported saved trip snapshot", e)
    }

    private fun location(value: Departure) = JsonObject().apply {
        addProperty("name", value.name)
        addProperty("latitude", value.latitude)
        addProperty("longitude", value.longitude)
    }

    private fun readLocation(value: JsonObject): Departure {
        requireKeys(value, "name", "latitude", "longitude")
        return Departure(string(value, "name"), number(value, "latitude").toDouble(), number(value, "longitude").toDouble())
    }

    private fun requireKeys(value: JsonObject, vararg expected: String) {
        require(value.keySet() == expected.toSet()) { "Missing or unexpected snapshot fields" }
    }

    private fun preferences(values: Set<PlacePreference>) = JsonArray().apply {
        values.map { it.name }.sorted().forEach { add(it) }
    }

    private fun readPreferences(values: JsonArray): Set<PlacePreference> {
        val result = values.map { value -> PlacePreference.valueOf(text(value)) }
        require(result.distinct().size == result.size) { "Duplicate preferences" }
        return result.toSet()
    }

    private fun text(value: JsonElement): String {
        require(value.isJsonPrimitive && value.asJsonPrimitive.isString)
        return value.asString
    }

    private fun string(value: JsonObject, key: String): String = text(requireNotNull(value.get(key)))

    private fun number(value: JsonObject, key: String): java.math.BigDecimal {
        val element = requireNotNull(value.get(key))
        require(element.isJsonPrimitive && element.asJsonPrimitive.isNumber)
        return element.asBigDecimal
    }
}
