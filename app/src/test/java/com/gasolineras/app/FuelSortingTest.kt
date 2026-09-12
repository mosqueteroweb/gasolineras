package com.gasolineras.app

import com.gasolineras.app.domain.model.FuelType
import com.gasolineras.app.domain.model.GasStation
import com.gasolineras.app.domain.model.SortOption
import org.junit.Assert.assertEquals
import org.junit.Test

class FuelSortingTest {

    private fun createStation(id: String, brand: String, priceG95: Double, distanceMeters: Double): GasStation {
        return GasStation(
            id = id,
            brand = brand,
            address = "Test Street",
            postalCode = "28001",
            municipality = "Madrid",
            province = "Madrid",
            schedule = "24H",
            latitude = 40.0,
            longitude = -3.0,
            prices = mapOf(FuelType.GASOLINA_95_E5 to priceG95),
            distanceMeters = distanceMeters
        )
    }

    @Test
    fun testSortByCheapest() {
        val s1 = createStation("1", "Repsol", 1.65, 1000.0)
        val s2 = createStation("2", "Plenoil", 1.45, 3000.0)
        val s3 = createStation("3", "Cepsa", 1.55, 500.0)

        val list = listOf(s1, s2, s3)
        val sorted = list.sortedWith(
            compareBy<GasStation> { it.prices[FuelType.GASOLINA_95_E5] ?: Double.MAX_VALUE }
                .thenBy { it.distanceMeters ?: Double.MAX_VALUE }
        )

        assertEquals("2", sorted[0].id) // Plenoil 1.45
        assertEquals("3", sorted[1].id) // Cepsa 1.55
        assertEquals("1", sorted[2].id) // Repsol 1.65
    }

    @Test
    fun testSortByNearest() {
        val s1 = createStation("1", "Repsol", 1.65, 1000.0)
        val s2 = createStation("2", "Plenoil", 1.45, 3000.0)
        val s3 = createStation("3", "Cepsa", 1.55, 500.0)

        val list = listOf(s1, s2, s3)
        val sorted = list.sortedWith(
            compareBy<GasStation> { it.distanceMeters ?: Double.MAX_VALUE }
                .thenBy { it.prices[FuelType.GASOLINA_95_E5] ?: Double.MAX_VALUE }
        )

        assertEquals("3", sorted[0].id) // Cepsa 500m
        assertEquals("1", sorted[1].id) // Repsol 1000m
        assertEquals("2", sorted[2].id) // Plenoil 3000m
    }

    @Test
    fun testGLPDetectionAndFiltering() {
        val s1 = GasStation(
            id = "1",
            brand = "Repsol",
            address = "Calle 1",
            postalCode = "28001",
            municipality = "Madrid",
            province = "Madrid",
            schedule = "24H",
            latitude = 40.0,
            longitude = -3.0,
            prices = mapOf(
                FuelType.GASOLINA_95_E5 to 1.65,
                FuelType.GLP to 0.949
            )
        )
        val s2 = GasStation(
            id = "2",
            brand = "Plenoil",
            address = "Calle 2",
            postalCode = "28002",
            municipality = "Madrid",
            province = "Madrid",
            schedule = "24H",
            latitude = 40.0,
            longitude = -3.0,
            prices = mapOf(
                FuelType.GASOLINA_95_E5 to 1.45
                // No GLP
            )
        )

        assertEquals(true, s1.hasGLP)
        assertEquals(false, s2.hasGLP)
        assertEquals(0.949, s1.glpPrice!!, 0.0001)
        assertEquals("0.949 €/L", s1.formattedGlpPrice)

        val stations = listOf(s1, s2)
        val glpOnly = stations.filter { it.hasGLP }
        assertEquals(1, glpOnly.size)
        assertEquals("1", glpOnly[0].id)
    }
}
