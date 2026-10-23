package com.gasolineras.app

import com.gasolineras.app.data.api.FuelStationDto
import com.gasolineras.app.data.local.StationCache
import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.util.DistanceCalculator
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class AuditSecurityTest {
    private fun station(lat: String = "40", lon: String = "-3", price: String = "1,5") =
        FuelStationDto(id = "1", latitud = lat, longitud = lon, precioG95 = price)

    @Test fun rejectsNonFiniteNumbersAndOversizedFields() {
        listOf("NaN", "Infinity", "-Infinity", "1e309", "9".repeat(100)).forEach {
            assertNull(FuelStationDto.parseEuropeanNumber(it))
        }
    }
    @Test fun rejectsCoordinatesOutsideEarthAndMissingIdentifiers() {
        assertNull(station("91").toDomain())
        assertNull(station(lon = "181").toDomain())
        assertNull(station().copy(id = "").toDomain())
        assertNull(station().copy(id = "x".repeat(65)).toDomain())
    }
    @Test fun ignoresInvalidPricesWithoutDiscardingOtherStationData() {
        listOf("NaN", "Infinity", "-1", "0", "1001").forEach {
            assertFalse(station(price = it).toDomain()!!.prices.containsKey(FuelType.GASOLINA_95_E5))
        }
    }
    @Test fun limitsDisplayTextFromExternalSources() {
        val result = station().copy(rotulo = "x".repeat(10000), direccion = "y".repeat(10000)).toDomain()!!
        assertEquals(256, result.brand.length)
        assertEquals(512, result.address.length)
    }
    @Test fun pricesUseSpanishDecimalsAndAppropriateUnits() {
        assertEquals("1,500 €/L", FuelType.GASOLINA_95_E5.formatPrice(1.5))
        assertEquals("1,500 €/kg", FuelType.GNC.formatPrice(1.5))
        assertEquals("1,500 €/kg", FuelType.GNL.formatPrice(1.5))
        assertEquals("12,500 €/kg", FuelType.HIDROGENO.formatPrice(12.5))
    }
    @Test fun oversizedDiskCacheIsRejectedWithoutParsing() {
        val file = File.createTempFile("gass-limit", ".json")
        try {
            java.io.RandomAccessFile(file, "rw").use { it.setLength(33L * 1024 * 1024) }
            assertNull(StationCache(file).read())
            StationCache(file).clear()
            assertFalse(file.exists())
        } finally { file.delete() }
    }
    @Test fun antipodalDistanceRemainsFinite() {
        assertTrue(DistanceCalculator.calculateDistanceMeters(0.0, 0.0, 0.0, 180.0).isFinite())
    }

    @Test fun publicationAgeDoesNotBecomeFreshJustBecauseItWasDownloadedAgain() {
        val freshness = com.gasolineras.app.domain.util.DataFreshness
        val now = java.time.ZonedDateTime.parse("2026-10-23T12:00:00+02:00[Europe/Madrid]").toInstant().toEpochMilli()
        assertTrue(freshness.isPublicationOld("21/10/2026 12:00:00", now))
        assertFalse(freshness.isPublicationOld("23/10/2026 11:00:00", now))
        assertFalse(freshness.isPublicationOld("31/02/2026 11:00:00", now))
    }
}
