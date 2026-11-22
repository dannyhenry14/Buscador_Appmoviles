package com.example.buscador.remote

import com.google.gson.annotations.SerializedName

/** DTO raíz de la respuesta de Open-Meteo. */
data class ClimaResponseDto(
    @SerializedName("current_weather") val climaActual: ClimaActualDto?
)

/** DTO para los datos meteorológicos en sí (temperatura, viento, código). */
data class ClimaActualDto(
    @SerializedName("temperature") val temperature: Double,
    @SerializedName("windspeed") val windspeed: Double,
    @SerializedName("weathercode") val weathercode: Int
)