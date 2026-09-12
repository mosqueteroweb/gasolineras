package com.gasolineras.app

import com.gasolineras.app.data.api.FuelStationDto
import com.gasolineras.app.domain.model.FuelType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FuelStationDtoTest {

    @Test
    fun testParseEuropeanNumber() {
        assertEquals(1.749, FuelStationDto.parseEuropeanNumber("1,749")!!, 0.0001)
        assertEquals(40.416775, FuelStationDto.parseEuropeanNumber("40,416775")!!, 0.000001)
        assertEquals(-3.70379, FuelStationDto.parseEuropeanNumber("-3,70379")!!, 0.000001)
        assertNull(FuelStationDto.parseEuropeanNumber(""))
        assertNull(FuelStationDto.parseEuropeanNumber("   "))
        assertNull(FuelStationDto.parseEuropeanNumber(null))
        assertNull(FuelStationDto.parseEuropeanNumber("invalid_text"))
    }

    @Test
    fun testToDomainMapping() {
        val dto = FuelStationDto(
            id = "1234",
            rotulo = "REPSOL",
            direccion = "CALLE ALCALA, 10",
            cp = "28014",
            municipio = "MADRID",
            provincia = "MADRID",
            horario = "L-D: 24H",
            latitud = "40,416775",
            longitud = "-3,703790",
            precioG95 = "1,659",
            precioDiesel = "1,529",
            precioG98 = "1,799",
            precioDieselPremium = "1,619",
            precioGLP = "0,959",
            precioGNC = null,
            precioGNL = null,
            precioBiodiesel = null,
            tipoVenta = "P",
            margen = "D"
        )

        val domain = dto.toDomain()
        assertNotNull(domain)
        domain?.let {
            assertEquals("1234", it.id)
            assertEquals("Repsol", it.cleanBrand)
            assertEquals("CALLE ALCALA, 10", it.address)
            assertEquals(40.416775, it.latitude, 0.00001)
            assertEquals(-3.703790, it.longitude, 0.00001)
            assertEquals(1.659, it.priceFor(FuelType.GASOLINA_95_E5)!!, 0.0001)
            assertEquals(1.529, it.priceFor(FuelType.GASOLEO_A)!!, 0.0001)
            assertEquals(true, it.isOpen24Hours)
            assertEquals("Repsol", it.shortBrand)
        }
    }

    @Test
    fun testNewFuelsMapping() {
        val dto = FuelStationDto(
            id = "5678",
            rotulo = "CEPSA",
            latitud = "40,400000",
            longitud = "-3,700000",
            precioGasoleoB = "1,199",
            precioAdblue = "0,750",
            precioDieselRenovable = "1,699",
            precioHidrogeno = "12,500"
        )
        val domain = dto.toDomain()
        assertNotNull(domain)
        domain?.let {
            assertEquals(1.199, it.priceFor(FuelType.GASOLEO_B)!!, 0.0001)
            assertEquals(0.750, it.priceFor(FuelType.ADBLUE)!!, 0.0001)
            assertEquals(1.699, it.priceFor(FuelType.DIESEL_RENOVABLE)!!, 0.0001)
            assertEquals(12.500, it.priceFor(FuelType.HIDROGENO)!!, 0.0001)
        }
    }

    @Test
    fun testBrandCleaningAndLengthLimit() {
        val dto1 = FuelStationDto(
            id = "1", rotulo = "ESTACION DE SERVICIO MONTALBAN", direccion = "Dir", cp = "28001",
            municipio = "Madrid", provincia = "Madrid", horario = "24H", latitud = "40.0", longitud = "-3.0",
            precioG95 = "1.5", precioDiesel = null, precioG98 = null, precioDieselPremium = null,
            precioGLP = null, precioGNC = null, precioGNL = null, precioBiodiesel = null,
            tipoVenta = "P", margen = "D"
        )
        val s1 = dto1.toDomain()!!
        assertEquals("Montalban", s1.cleanBrand)
        assertEquals("Montalban", s1.shortBrand)
        assert(s1.shortBrand.length <= 10)

        val dto2 = FuelStationDto(
            id = "2", rotulo = "COOPERATIVA AGRICOLA SAN ISIDRO", direccion = "Dir", cp = "28001",
            municipio = "Madrid", provincia = "Madrid", horario = "24H", latitud = "40.0", longitud = "-3.0",
            precioG95 = "1.5", precioDiesel = null, precioG98 = null, precioDieselPremium = null,
            precioGLP = null, precioGNC = null, precioGNL = null, precioBiodiesel = null,
            tipoVenta = "P", margen = "D"
        )
        val s2 = dto2.toDomain()!!
        assert(s2.shortBrand.length <= 10)

        val dto3 = FuelStationDto(
            id = "3", rotulo = "E.S. REPSOL", direccion = "Dir", cp = "28001",
            municipio = "Madrid", provincia = "Madrid", horario = "24H", latitud = "40.0", longitud = "-3.0",
            precioG95 = "1.5", precioDiesel = null, precioG98 = null, precioDieselPremium = null,
            precioGLP = null, precioGNC = null, precioGNL = null, precioBiodiesel = null,
            tipoVenta = "P", margen = "D"
        )
        val s3 = dto3.toDomain()!!
        assertEquals("Repsol", s3.cleanBrand)
        assertEquals("Repsol", s3.shortBrand)
    }

    @Test
    fun testToDomainWithInvalidCoordinatesReturnsNull() {
        val invalidDto = FuelStationDto(
            id = "999",
            rotulo = "TEST",
            direccion = null,
            cp = null,
            municipio = null,
            provincia = null,
            horario = null,
            latitud = "invalida",
            longitud = "-3,70",
            precioG95 = "1,50",
            precioDiesel = null,
            precioG98 = null,
            precioDieselPremium = null,
            precioGLP = null,
            precioGNC = null,
            precioGNL = null,
            precioBiodiesel = null,
            tipoVenta = "P",
            margen = null
        )

        val domain = invalidDto.toDomain()
        assertNull(domain)
    }
}
