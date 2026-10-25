package com.gasolineras.app

import com.gasolineras.app.domain.util.ReadableText
import org.junit.Assert.assertEquals
import org.junit.Test

class ReadableTextTest {
    @Test fun capitalisedAddressesBecomeReadable() {
        assertEquals("Avenida Democracia, 15", ReadableText.toReadableCase("AVENIDA DEMOCRACIA, 15"))
        assertEquals("Calle de la Princesa, 3", ReadableText.toReadableCase("CALLE DE LA PRINCESA, 3"))
        assertEquals("Pozuelo de Alarcón", ReadableText.toReadableCase("POZUELO DE ALARCÓN"))
    }

    @Test fun roadCodesAndNumbersKeepTheirCapitals() {
        assertEquals("Carretera N-II Km 12", ReadableText.toReadableCase("CARRETERA N-II KM 12"))
        assertEquals("Autovía M-30, S/N", ReadableText.toReadableCase("AUTOVÍA M-30, S/N"))
        assertEquals("Avenida de la Constitución III", ReadableText.toReadableCase("AVENIDA DE LA CONSTITUCIÓN III"))
    }

    @Test fun textThatAlreadyHasLowercaseIsLeftAlone() {
        assertEquals("Área 117", ReadableText.toReadableCase("Área 117"))
    }

    @Test fun aStrayLowercaseUnitDoesNotKeepTheWholeAddressInCapitals() {
        assertEquals("Avda. Princesa Juana de Austria Km 4,700 km 4.700",
            ReadableText.toReadableCase("AVDA. PRINCESA JUANA DE AUSTRIA KM 4,700 km 4.700"))
    }

    @Test fun provinceIsOnlyShownWhenItAddsInformation() {
        assertEquals("Madrid", ReadableText.place("Madrid", "MADRID"))
        assertEquals("Getafe (Madrid)", ReadableText.place("Getafe", "MADRID"))
        assertEquals("Ávila", ReadableText.place("ÁVILA", "AVILA"))
        assertEquals("Lugo", ReadableText.place("LUGO", ""))
    }
}
