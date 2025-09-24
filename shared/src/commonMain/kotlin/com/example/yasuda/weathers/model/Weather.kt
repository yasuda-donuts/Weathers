package com.example.yasuda.weathers.model

enum class Weather {
    CLEAR_SKY,
    PARTLY_CLOUDY,
    FOG,
    DRIZZLE,
    RAIN,
    SNOW,
    SHOWERS,
    THUNDERSTORM,
    UNKNOWN;

    companion object {
        /**
         * Gets the [Weather] corresponding to the given WMO weather code.
         * @param code The WMO weather code.
         * @return The corresponding [Weather].
         */
        fun fromWmoCode(code: Int): Weather {
            return when (code) {
                0 -> CLEAR_SKY
                1, 2, 3 -> PARTLY_CLOUDY
                45, 48 -> FOG
                51, 53, 55 -> DRIZZLE
                61, 63, 65 -> RAIN
                71, 73, 75 -> SNOW
                80, 81, 82 -> SHOWERS
                95, 96, 99 -> THUNDERSTORM
                else -> UNKNOWN
            }
        }
    }
}
