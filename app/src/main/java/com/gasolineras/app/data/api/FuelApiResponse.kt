package com.gasolineras.app.data.api

import com.google.gson.annotations.SerializedName

data class FuelApiResponse(
    @SerializedName("Fecha")
    val date: String?,

    @SerializedName("ListaEESSPrecio")
    val stations: List<FuelStationDto>?
)
