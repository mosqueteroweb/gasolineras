package com.gasolineras.app.domain.model

import com.gasolineras.app.domain.util.DistanceCalculator

data class GasStation(
    val id: String,
    val brand: String,
    val address: String,
    val postalCode: String,
    val municipality: String,
    val province: String,
    val schedule: String,
    val latitude: Double,
    val longitude: Double,
    val prices: Map<FuelType, Double>,
    val distanceMeters: Double? = null,
    val saleType: String = "P",
    val margin: String = "I"
) {
    val formattedDistance: String
        get() = DistanceCalculator.formatDistance(distanceMeters)

    val distanceKm: Double?
        get() = distanceMeters?.let { it / 1000.0 }

    val isOpen24Hours: Boolean
        get() = schedule.contains("24H", ignoreCase = true) || schedule.contains("24 HORAS", ignoreCase = true)

    val hasGLP: Boolean
        get() = prices.containsKey(FuelType.GLP) && (prices[FuelType.GLP] ?: 0.0) > 0.0

    val glpPrice: Double?
        get() = prices[FuelType.GLP]

    val formattedGlpPrice: String
        get() = glpPrice?.let { String.format(java.util.Locale.US, "%.3f €/L", it) } ?: "--"

    fun priceFor(fuelType: FuelType): Double? = prices[fuelType]

    fun formattedPriceFor(fuelType: FuelType): String {
        val price = prices[fuelType] ?: return "--"
        return String.format(java.util.Locale.US, "%.3f €/L", price)
    }

    /**
     * Determines a clean display brand name (e.g. cleans up prefixes).
     */
    val cleanBrand: String
        get() {
            val trimmed = brand.trim()
            return when {
                trimmed.startsWith("REPSOL", ignoreCase = true) -> "Repsol"
                trimmed.startsWith("CEPSA", ignoreCase = true) -> "Cepsa"
                trimmed.startsWith("BP", ignoreCase = true) -> "BP"
                trimmed.startsWith("GALP", ignoreCase = true) -> "Galp"
                trimmed.startsWith("SHELL", ignoreCase = true) -> "Shell"
                trimmed.startsWith("PLENOIL", ignoreCase = true) -> "Plenoil"
                trimmed.startsWith("BALLENOIL", ignoreCase = true) -> "Ballenoil"
                trimmed.startsWith("PETROPRIX", ignoreCase = true) -> "Petroprix"
                trimmed.startsWith("AVIA", ignoreCase = true) -> "Avia"
                trimmed.startsWith("DISA", ignoreCase = true) -> "Disa"
                trimmed.startsWith("CAMPSA", ignoreCase = true) -> "Campsa"
                trimmed.startsWith("ALCAMPO", ignoreCase = true) -> "Alcampo"
                trimmed.startsWith("CARREFOUR", ignoreCase = true) -> "Carrefour"
                trimmed.startsWith("EROSKI", ignoreCase = true) -> "Eroski"
                trimmed.startsWith("ESCLAT", ignoreCase = true) -> "EsclatOil"
                trimmed.startsWith("BONAREA", ignoreCase = true) -> "BonÁrea"
                trimmed.isBlank() -> "Gasolinera"
                else -> trimmed.lowercase().split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }
}
