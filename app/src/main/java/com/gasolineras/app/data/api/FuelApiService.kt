package com.gasolineras.app.data.api

import retrofit2.http.GET
import retrofit2.http.Path

interface FuelApiService {

    /**
     * Downloads all terrestrial fuel stations in Spain with up-to-date fuel prices.
     */
    @GET("PreciosCarburantes/EstacionesTerrestres/")
    suspend fun getAllStations(): FuelApiResponse

    /**
     * Downloads fuel stations filtered by Spanish province ID (e.g. "28" for Madrid, "08" for Barcelona).
     */
    @GET("PreciosCarburantes/EstacionesTerrestres/FiltroProvincia/{idProvincia}")
    suspend fun getStationsByProvince(
        @Path("idProvincia") idProvincia: String
    ): FuelApiResponse
}
