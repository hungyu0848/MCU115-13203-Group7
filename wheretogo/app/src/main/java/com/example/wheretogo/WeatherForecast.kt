package com.example.wheretogo

import com.google.gson.JsonParser
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

data class HourlyWeather(
    val timeEpochSeconds: Long,
    val timeZone: String,
    val temperatureC: Double?,
    val condition: WeatherCondition,
    val precipitationProbability: Int?
)

/** Open-Meteo forecast values are hourly model estimates, not observations. */
object WeatherForecast {
    fun requestUrl(latitude: Double, longitude: Double): String {
        require(latitude.isFinite() && latitude in -90.0..90.0)
        require(longitude.isFinite() && longitude in -180.0..180.0)
        return "https://api.open-meteo.com/v1/forecast?latitude=$latitude&longitude=$longitude" +
            "&hourly=temperature_2m,weather_code,precipitation_probability&timeformat=unixtime&timezone=auto&forecast_days=2"
    }

    /** Return the nearest supplied hour, but never substitute an out-of-range forecast. */
    fun parseNearest(json: String, targetEpochSeconds: Long): HourlyWeather {
        try {
            val root = JsonParser.parseString(json).asJsonObject
            val zone = root.get("timezone").asString
            require(zone.isNotBlank() && java.util.TimeZone.getAvailableIDs().contains(zone))
            val hourly = root.getAsJsonObject("hourly")
            val times = hourly.getAsJsonArray("time")
            val temperatures = hourly.get("temperature_2m")?.takeUnless { it.isJsonNull }?.asJsonArray
            val codes = hourly.get("weather_code")?.takeUnless { it.isJsonNull }?.asJsonArray
            val probabilities = hourly.get("precipitation_probability")?.takeUnless { it.isJsonNull }?.asJsonArray
            require(times.size() > 0 && listOfNotNull(temperatures, codes, probabilities).all { it.size() == times.size() })
            val epochs = times.map { it.asBigDecimal.longValueExact() }
            require(epochs.all { it in 0..253402300799L })
            require(epochs.zipWithNext().all { (a, b) -> a < b })
            require(targetEpochSeconds >= epochs.first() && targetEpochSeconds <= epochs.last() + 3599)
            val index = epochs.indices.minBy { abs(epochs[it] - targetEpochSeconds) }
            // A gap or the end of the forecast must not silently substitute a distant hour.
            require(abs(epochs[index] - targetEpochSeconds) <= 1800)
            val temp = temperatures?.get(index)?.takeUnless { it.isJsonNull }?.asDouble
            val probability = probabilities?.get(index)?.takeUnless { it.isJsonNull }?.asBigDecimal?.intValueExact()
            val code = codes?.get(index)?.takeUnless { it.isJsonNull }?.asBigDecimal?.intValueExact()
            require(temp == null || temp.isFinite())
            require(probability == null || probability in 0..100)
            return HourlyWeather(epochs[index], zone, temp,
                code?.let(WeatherCondition::fromWmoCode) ?: WeatherCondition.UNKNOWN, probability)
        } catch (e: Exception) {
            throw IOException("Invalid or unavailable weather forecast", e)
        }
    }

    fun fetch(latitude: Double, longitude: Double, targetEpochSeconds: Long): HourlyWeather {
        val connection = URL(requestUrl(latitude, longitude)).openConnection() as HttpURLConnection
        try {
            connection.connectTimeout = 5000
            connection.readTimeout = 5000
            connection.setRequestProperty("User-Agent", "WhereToGoStudentProject/1.0")
            if (connection.responseCode != 200) throw IOException("Weather HTTP ${connection.responseCode}")
            val body = connection.inputStream.bufferedReader().use { reader ->
                val buffer = CharArray(4096)
                val result = StringBuilder()
                while (true) {
                    val count = reader.read(buffer)
                    if (count < 0) break
                    if (result.length + count > 200_000) throw IOException("Weather response too large")
                    result.append(buffer, 0, count)
                }
                result.toString()
            }
            return parseNearest(body, targetEpochSeconds)
        } finally { connection.disconnect() }
    }
}
