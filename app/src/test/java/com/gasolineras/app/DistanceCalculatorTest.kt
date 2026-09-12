package com.gasolineras.app

import com.gasolineras.app.domain.util.DistanceCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceCalculatorTest {

    @Test
    fun testSameLocationDistanceIsZero() {
        val distance = DistanceCalculator.calculateDistanceMeters(
            40.4168, -3.7038,
            40.4168, -3.7038
        )
        assertEquals(0.0, distance, 0.001)
    }

    @Test
    fun testMadridPuertaDelSolToAtocha() {
        // Puerta del Sol: 40.416775, -3.703790
        // Estación de Atocha: 40.4065, -3.6896
        val distance = DistanceCalculator.calculateDistanceMeters(
            40.416775, -3.703790,
            40.4065, -3.6896
        )
        // Distance is ~1.6 km (between 1400m and 1800m)
        assertTrue("Distance should be approximately 1.6km, was $distance", distance in 1400.0..1800.0)
    }

    @Test
    fun testFormatDistance() {
        assertEquals("450 m", DistanceCalculator.formatDistance(450.0))
        assertEquals("1.5 km", DistanceCalculator.formatDistance(1500.0))
        assertEquals("12.0 km", DistanceCalculator.formatDistance(12000.0))
        assertEquals("-- km", DistanceCalculator.formatDistance(null))
    }
}
