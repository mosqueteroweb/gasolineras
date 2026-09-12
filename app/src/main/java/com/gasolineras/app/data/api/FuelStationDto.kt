package com.gasolineras.app.data.api

import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.google.gson.annotations.SerializedName

data class FuelStationDto(
    @SerializedName("IDEESS")
    val id: String?,

    @SerializedName("Rótulo")
    val rotulo: String?,

    @SerializedName("Dirección")
    val direccion: String?,

    @SerializedName("C.P.")
    val cp: String?,

    @SerializedName("Municipio")
    val municipio: String?,

    @SerializedName("Provincia")
    val provincia: String?,

    @SerializedName("Horario")
    val horario: String?,

    @SerializedName("Latitud")
    val latitud: String?,

    @SerializedName("Longitud (WGS84)")
    val longitud: String?,

    @SerializedName("Precio Gasolina 95 E5")
    val precioG95: String?,

    @SerializedName("Precio Gasoleo A")
    val precioDiesel: String?,

    @SerializedName("Precio Gasolina 98 E5")
    val precioG98: String?,

    @SerializedName("Precio Gasoleo Premium")
    val precioDieselPremium: String?,

    @SerializedName("Precio Gases licuados del petróleo")
    val precioGLP: String?,

    @SerializedName("Precio Gas Natural Comprimido")
    val precioGNC: String?,

    @SerializedName("Precio Gas Natural Licuado")
    val precioGNL: String?,

    @SerializedName("Precio Biodiesel")
    val precioBiodiesel: String?,

    @SerializedName("Tipo Venta")
    val tipoVenta: String?,

    @SerializedName("Margen")
    val margen: String?
) {
    /**
     * Converts the DTO into a clean domain GasStation, parsing European comma decimals safely.
     */
    fun toDomain(): GasStation? {
        val lat = parseEuropeanNumber(latitud) ?: return null
        val lon = parseEuropeanNumber(longitud) ?: return null

        val priceMap = mutableMapOf<FuelType, Double>()

        parseEuropeanNumber(precioG95)?.let { priceMap[FuelType.GASOLINA_95_E5] = it }
        parseEuropeanNumber(precioDiesel)?.let { priceMap[FuelType.GASOLEO_A] = it }
        parseEuropeanNumber(precioG98)?.let { priceMap[FuelType.GASOLINA_98_E5] = it }
        parseEuropeanNumber(precioDieselPremium)?.let { priceMap[FuelType.GASOLEO_PREMIUM] = it }
        parseEuropeanNumber(precioGLP)?.let { priceMap[FuelType.GLP] = it }
        parseEuropeanNumber(precioGNC)?.let { priceMap[FuelType.GNC] = it }
        parseEuropeanNumber(precioGNL)?.let { priceMap[FuelType.GNL] = it }
        parseEuropeanNumber(precioBiodiesel)?.let { priceMap[FuelType.BIODIESEL] = it }

        return GasStation(
            id = id.orEmpty(),
            brand = rotulo?.trim().orEmpty().ifEmpty { "Gasolinera" },
            address = direccion?.trim().orEmpty(),
            postalCode = cp?.trim().orEmpty(),
            municipality = municipio?.trim().orEmpty(),
            province = provincia?.trim().orEmpty(),
            schedule = horario?.trim().orEmpty().ifEmpty { "Consultar horario" },
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
                value.trim().replace(',', '.').toDouble()
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}
