package com.gasolineras.app.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer
import java.io.IOException
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val BASE_URL = "https://sedeaplicaciones.minetur.gob.es/ServiciosRESTCarburantes/"

    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val response = chain.proceed(chain.request())
                val original = response.body
                if (original == null) response else {
                    val limit = 32L * 1024 * 1024
                    if (original.contentLength() > limit) {
                        response.close()
                        throw IOException("Respuesta demasiado grande")
                    }
                    val bounded = object : ResponseBody() {
                        private val boundedSource = object : ForwardingSource(original.source()) {
                            var total = 0L
                            override fun read(sink: Buffer, byteCount: Long): Long {
                                val count = super.read(sink, minOf(byteCount, limit - total + 1))
                                if (count > 0) total += count
                                if (total > limit) throw IOException("Respuesta demasiado grande")
                                return count
                            }
                        }.buffer()
                        override fun contentType() = original.contentType()
                        override fun contentLength() = original.contentLength()
                        override fun source(): BufferedSource = boundedSource
                    }
                    response.newBuilder().body(bounded).build()
                }
            }
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (com.gasolineras.app.BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            builder.addInterceptor(logging)
        }

        builder.build()
    }

    private val gson = GsonBuilder()
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val fuelApiService: FuelApiService by lazy {
        retrofit.create(FuelApiService::class.java)
    }
}
