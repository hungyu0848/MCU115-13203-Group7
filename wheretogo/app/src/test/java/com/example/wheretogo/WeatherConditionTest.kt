package com.example.wheretogo

import org.junit.Assert.assertEquals
import org.junit.Test

class WeatherConditionTest {
    @Test fun recognizedCodesMatchDocumentedWeatherConditions() {
        val examples = mapOf(
            0 to WeatherCondition.CLEAR, 2 to WeatherCondition.PARTLY_CLOUDY,
            48 to WeatherCondition.RIME_FOG, 57 to WeatherCondition.DENSE_FREEZING_DRIZZLE,
            65 to WeatherCondition.HEAVY_RAIN, 67 to WeatherCondition.HEAVY_FREEZING_RAIN,
            77 to WeatherCondition.SNOW_GRAINS, 82 to WeatherCondition.HEAVY_RAIN_SHOWERS,
            86 to WeatherCondition.HEAVY_SNOW_SHOWERS, 97 to WeatherCondition.HEAVY_THUNDERSTORM,
            99 to WeatherCondition.THUNDERSTORM_HEAVY_HAIL
        )
        examples.forEach { (code, expected) -> assertEquals(expected, WeatherCondition.fromWmoCode(code)) }
    }

    @Test fun unsupportedCodesAreNeverTreatedAsKnownWeather() {
        listOf(-1, 4, 44, 46, 52, 98, 100, Int.MAX_VALUE).forEach {
            assertEquals(WeatherCondition.UNKNOWN, WeatherCondition.fromWmoCode(it))
        }
    }
}
