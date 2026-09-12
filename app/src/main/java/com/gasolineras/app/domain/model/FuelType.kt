package com.gasolineras.app.domain.model

enum class FuelType(
    val code: String,
    val displayName: String,
    val shortName: String
) {
    GASOLINA_95_E5(
        code = "Precio Gasolina 95 E5",
        displayName = "Gasolina 95 E5",
        shortName = "G95"
    ),
    GASOLEO_A(
        code = "Precio Gasoleo A",
        displayName = "Diésel (Gasóleo A)",
        shortName = "Diésel"
    ),
    GASOLINA_98_E5(
        code = "Precio Gasolina 98 E5",
        displayName = "Gasolina 98 E5",
        shortName = "G98"
    ),
    GASOLEO_PREMIUM(
        code = "Precio Gasoleo Premium",
        displayName = "Diésel Premium",
        shortName = "Diésel+"
    ),
    GLP(
        code = "Precio Gases licuados del petróleo",
        displayName = "GLP (Autogas)",
        shortName = "GLP"
    ),
    GNC(
        code = "Precio Gas Natural Comprimido",
        displayName = "GNC (Gas Comprimido)",
        shortName = "GNC"
    ),
    GNL(
        code = "Precio Gas Natural Licuado",
        displayName = "GNL (Gas Licuado)",
        shortName = "GNL"
    ),
    BIODIESEL(
        code = "Precio Biodiesel",
        displayName = "Biodiésel",
        shortName = "Bio"
    );

    companion object {
        val default = GASOLINA_95_E5
    }
}
