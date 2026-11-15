package com.example.wheretogo

import org.junit.Assert.*
import org.junit.Test
import java.io.IOException

class WeatherForecastTest {
    private val json = """{"timezone":"Asia/Taipei","hourly":{"time":[3600,7200,10800],"temperature_2m":[21.5,null,23.0],"weather_code":[1,99,999],"precipitation_probability":[10,null,80]}}"""

    @Test fun selectsNearestHourAndKeepsMissingDataExplicit() {
        val weather = WeatherForecast.parseNearest(json, 7000)
        assertEquals(7200L, weather.timeEpochSeconds)
        assertEquals("Asia/Taipei", weather.timeZone)
        assertEquals(WeatherCondition.THUNDERSTORM_HEAVY_HAIL, weather.condition)
        assertNull(weather.temperatureC)
        assertNull(weather.precipitationProbability)
        assertEquals(WeatherCondition.UNKNOWN, WeatherForecast.parseNearest(json, 10800).condition)
    }

    @Test fun rejectsOutOfRangeAndMalformedData() {
        assertThrows(IOException::class.java) { WeatherForecast.parseNearest(json, 3599) }
        assertThrows(IOException::class.java) { WeatherForecast.parseNearest(json, 14400) }
        assertThrows(IOException::class.java) { WeatherForecast.parseNearest("{}", 7200) }
        assertThrows(IOException::class.java) { WeatherForecast.parseNearest(json.replace("[10,null,80]", "[101,null,80]"), 3600) }
        assertThrows(IOException::class.java) { WeatherForecast.parseNearest(json.replace("[10,null,80]", "[10.5,null,80]"), 3600) }
        assertThrows(IOException::class.java) { WeatherForecast.parseNearest(json.replace("[3600,7200,10800]", "[7200,3600,10800]"), 7200) }
    }

    @Test fun missingOptionalSeriesAreUnavailableRatherThanInvented() {
        val missing = """{"timezone":"Asia/Taipei","hourly":{"time":[3600]}}"""
        val weather = WeatherForecast.parseNearest(missing, 3600)
        assertNull(weather.temperatureC)
        assertNull(weather.precipitationProbability)
        assertEquals(WeatherCondition.UNKNOWN, weather.condition)
    }

    @Test fun urlUsesCoordinatesAndDoesNotDependOnLocale() {
        val prior = java.util.Locale.getDefault()
        try {
            java.util.Locale.setDefault(java.util.Locale.FRANCE)
            val url = WeatherForecast.requestUrl(25.2, 121.5)
            assertTrue(url.contains("latitude=25.2&longitude=121.5"))
            assertTrue(url.contains("timeformat=unixtime&timezone=auto"))
            assertThrows(IllegalArgumentException::class.java) { WeatherForecast.requestUrl(Double.NaN, 0.0) }
        } finally { java.util.Locale.setDefault(prior) }
    }
}
