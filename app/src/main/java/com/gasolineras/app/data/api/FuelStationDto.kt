package com.gasolineras.app.data.api

import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.google.gson.annotations.SerializedName

data class FuelStationDto(
    @SerializedName("IDEESS")
    val id: String? = null,

    @SerializedName("Rótulo")
    val rotulo: String? = null,

    @SerializedName("Dirección")
    val direccion: String? = null,

    @SerializedName("C.P.")
    val cp: String? = null,

    @SerializedName("Municipio")
    val municipio: String? = null,

    @SerializedName("Provincia")
    val provincia: String? = null,

    @SerializedName("Horario")
    val horario: String? = null,

    @SerializedName("Latitud")
    val latitud: String? = null,

    @SerializedName("Longitud (WGS84)")
    val longitud: String? = null,

    @SerializedName("Precio Gasolina 95 E5")
    val precioG95: String? = null,

    @SerializedName("Precio Gasoleo A")
    val precioDiesel: String? = null,

    @SerializedName("Precio Gasolina 98 E5")
    val precioG98: String? = null,

    @SerializedName("Precio Gasoleo Premium")
    val precioDieselPremium: String? = null,

    @SerializedName("Precio Gases licuados del petróleo")
    val precioGLP: String? = null,

    @SerializedName("Precio Gas Natural Comprimido")
    val precioGNC: String? = null,

    @SerializedName("Precio Gas Natural Licuado")
    val precioGNL: String? = null,

    @SerializedName("Precio Biodiesel")
    val precioBiodiesel: String? = null,

    @SerializedName("Precio Gasoleo B")
    val precioGasoleoB: String? = null,

    @SerializedName("Precio Adblue")
    val precioAdblue: String? = null,

    @SerializedName("Precio Diésel Renovable")
    val precioDieselRenovable: String? = null,

    @SerializedName("Precio Hidrogeno")
    val precioHidrogeno: String? = null,

    @SerializedName("Tipo Venta")
    val tipoVenta: String? = null,

    @SerializedName("Margen")
    val margen: String? = null
) {
    /**
     * Converts the DTO into a clean domain GasStation, parsing European comma decimals safely.
     */
    fun toDomain(): GasStation? {
        val lat = parseEuropeanNumber(latitud) ?: return null
        val lon = parseEuropeanNumber(longitud) ?: return null

        if (lat !in -90.0..90.0 || lon !in -180.0..180.0 || id.isNullOrBlank() || id.length > 64) return null

        val priceMap = mutableMapOf<FuelType, Double>()

        parseEuropeanNumber(precioG95)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GASOLINA_95_E5] = it }
        parseEuropeanNumber(precioDiesel)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GASOLEO_A] = it }
        parseEuropeanNumber(precioG98)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GASOLINA_98_E5] = it }
        parseEuropeanNumber(precioDieselPremium)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GASOLEO_PREMIUM] = it }
        parseEuropeanNumber(precioGLP)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GLP] = it }
        parseEuropeanNumber(precioGNC)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GNC] = it }
        parseEuropeanNumber(precioGNL)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GNL] = it }
        parseEuropeanNumber(precioBiodiesel)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.BIODIESEL] = it }
        parseEuropeanNumber(precioGasoleoB)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.GASOLEO_B] = it }
        parseEuropeanNumber(precioAdblue)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.ADBLUE] = it }
        parseEuropeanNumber(precioDieselRenovable)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.DIESEL_RENOVABLE] = it }
        parseEuropeanNumber(precioHidrogeno)?.takeIf { it > 0.0 && it <= 1000.0 }?.let { priceMap[FuelType.HIDROGENO] = it }

        return GasStation(
            id = id.orEmpty(),
            brand = rotulo?.take(256)?.trim().orEmpty().ifEmpty { "Gasolinera" },
            address = direccion?.take(512)?.trim().orEmpty(),
            postalCode = cp?.trim().orEmpty(),
            municipality = municipio?.take(256)?.trim().orEmpty(),
            province = provincia?.take(256)?.trim().orEmpty(),
            schedule = horario?.take(1024)?.trim().orEmpty().ifEmpty { "Consultar horario" },
            latitude = lat,
            longitude = lon,
            prices = priceMap,
            saleType = tipoVenta?.trim().orEmpty().ifEmpty { "P" },
            margin = margen?.trim().orEmpty().ifEmpty { "I" }
        )
    }

    companion object {
        fun parseEuropeanNumber(value: String?): Double? {
            if (value.isNullOrBlank()) return null
            return try {
                value.takeIf { it.length <= 32 }?.trim()?.replace(',', '.')?.toDouble()?.takeIf { it.isFinite() }
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}
