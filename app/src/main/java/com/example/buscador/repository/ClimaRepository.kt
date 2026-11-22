package com.example.buscador.repository

import android.util.Log
import com.example.buscador.model.ClimaActual
import com.example.buscador.remote.ClimaApi

/**
 * Contrato de acceso a la información meteorológica.
 */
interface ClimaRepository {
    /** Retorna el clima actual de Santiago o null si falla la red. */
    suspend fun obtenerClimaSantiago(): ClimaActual?
}

/**
 * Implementación que consulta Open-Meteo mediante Retrofit y traduce
 * el código numérico a español.
 */
class ClimaRepositoryImpl(private val api: ClimaApi) : ClimaRepository {

    private val etiqueta = "ClimaRepository"

    override suspend fun obtenerClimaSantiago(): ClimaActual? {
        return try {
            val respuesta = api.obtenerClima()
            val dto = respuesta.climaActual ?: return null
            
            val condicion = when (dto.weathercode) {
                0 -> "Despejado"
                1, 2, 3 -> "Parcialmente nublado"
                45, 48 -> "Neblina"
                51, 53, 55, 56, 57 -> "Llovizna"
                61, 63, 65, 66, 67 -> "Lluvia"
                71, 73, 75, 77 -> "Nieve"
                80, 81, 82 -> "Chubascos"
                95, 96, 99 -> "Tormenta"
                else -> "Desconocido"
            }
            
            ClimaActual(
                temperatura = dto.temperature,
                viento = dto.windspeed,
                condicion = condicion
            )
        } catch (e: Exception) {
            Log.e(etiqueta, "Error al obtener el clima actual", e)
            null
        }
    }
}