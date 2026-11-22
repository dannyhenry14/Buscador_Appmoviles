package com.example.buscador.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/** Endpoints públicos de Open-Meteo para obtener el clima actual. */
interface ClimaApi {
    @GET("v1/forecast")
    suspend fun obtenerClima(
        @Query("latitude") latitude: Double = -33.4489,
        @Query("longitude") longitude: Double = -70.6693,
        @Query("current_weather") currentWeather: Boolean = true
    ): ClimaResponseDto
}

/** Cliente de Retrofit independiente dedicado a la API de Open-Meteo. */
object ClimaRetrofitClient {
    private const val BASE_URL = "https://api.open-meteo.com/"
    
    fun crearApi(baseUrl: String = BASE_URL): ClimaApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ClimaApi::class.java)
    }
    
    val api: ClimaApi by lazy { crearApi() }
}