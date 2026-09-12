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
    val margin: String = "I",
    val isFavorite: Boolean = false
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
            val normalized = trimmed
                .replace(Regex("^(E\\.S\\.?|ESTACION DE SERVICIO|GASOLINERA|EESS|COOP\\.?|COOPERATIVA)\\s+", RegexOption.IGNORE_CASE), "")
                .trim()
            return when {
                normalized.startsWith("REPSOL", ignoreCase = true) -> "Repsol"
                normalized.startsWith("CEPSA", ignoreCase = true) -> "Cepsa"
                normalized.startsWith("BP", ignoreCase = true) -> "BP"
                normalized.startsWith("GALP", ignoreCase = true) -> "Galp"
                normalized.startsWith("SHELL", ignoreCase = true) -> "Shell"
                normalized.startsWith("PLENOIL", ignoreCase = true) -> "Plenoil"
                normalized.startsWith("BALLENOIL", ignoreCase = true) -> "Ballenoil"
                normalized.startsWith("PETROPRIX", ignoreCase = true) -> "Petroprix"
                normalized.startsWith("AVIA", ignoreCase = true) -> "Avia"
                normalized.startsWith("DISA", ignoreCase = true) -> "Disa"
                normalized.startsWith("CAMPSA", ignoreCase = true) -> "Campsa"
                normalized.startsWith("ALCAMPO", ignoreCase = true) -> "Alcampo"
                normalized.startsWith("CARREFOUR", ignoreCase = true) -> "Carrefour"
                normalized.startsWith("EROSKI", ignoreCase = true) -> "Eroski"
                normalized.startsWith("ESCLAT", ignoreCase = true) -> "EsclatOil"
                normalized.startsWith("BONAREA", ignoreCase = true) -> "BonÁrea"
                normalized.isBlank() -> "Gasolinera"
                else -> normalized.lowercase().split(" ").joinToString(" ") { word ->
                    word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                }
            }
        }

    /**
     * Short brand name capped to at most 10 characters for list view.
     */
    val shortBrand: String
        get() = if (cleanBrand.length > 10) cleanBrand.take(10).trim() else cleanBrand
}
