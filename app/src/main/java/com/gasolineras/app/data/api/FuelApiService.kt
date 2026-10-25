package com.gasolineras.app.data.api

import retrofit2.http.GET

interface FuelApiService {

    /**
     * Downloads all terrestrial fuel stations in Spain with up-to-date fuel prices.
     */
    @GET("PreciosCarburantes/EstacionesTerrestres/")
    suspend fun getAllStations(): FuelApiResponse
}
