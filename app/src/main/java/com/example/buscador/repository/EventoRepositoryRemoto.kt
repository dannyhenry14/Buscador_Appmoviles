package com.example.buscador.repository

import android.util.Log
import com.example.buscador.model.CategoriaEvento
import com.example.buscador.model.Evento
import com.example.buscador.remote.CenamadApi
import com.example.buscador.remote.aModeloDominio

/**
 * Repositorio de eventos que consume la API remota de Xano.
 */
class EventoRepositoryRemoto(
    private val api: CenamadApi
) : EventoRepository {

    private val etiqueta = "EventoRepositoryRemoto"

    override suspend fun obtenerTodosLosEventos(): List<Evento> {
        return try {
            api.obtenerEventos().map { it.aModeloDominio() }
        } catch (e: Exception) {
            Log.e(etiqueta, "Error al obtener todos los eventos", e)
            emptyList()
        }
    }

    override suspend fun obtenerEventoPorId(idEvento: Long): Evento? {
        return try {
            api.obtenerEventoPorId(idEvento.toInt()).aModeloDominio()
        } catch (e: Exception) {
            Log.e(etiqueta, "Error al obtener el evento $idEvento", e)
            null
        }
    }

    override suspend fun buscarEventos(
        texto: String,
        categoria: CategoriaEvento?,
        fechaDesdeMillis: Long?,
        fechaHastaMillis: Long?
    ): List<Evento> {
        // Obtenemos todos los eventos desde la API y aplicamos los filtros localmente.
        // En una API real con endpoints de filtrado, los parámetros se pasarían al backend.
        val textoLimpio = texto.trim()
        val todosLosEventos = obtenerTodosLosEventos()

        return todosLosEventos.filter { evento ->
            val coincideTexto = textoLimpio.isEmpty() ||
                    evento.titulo.contains(textoLimpio, ignoreCase = true) ||
                    evento.descripcion.contains(textoLimpio, ignoreCase = true)
            val coincideCategoria = categoria == null || evento.categoria == categoria
            val coincideDesde = fechaDesdeMillis == null || evento.fechaMillis >= fechaDesdeMillis
            val coincideHasta = fechaHastaMillis == null || evento.fechaMillis <= fechaHastaMillis
            
            coincideTexto && coincideCategoria && coincideDesde && coincideHasta
        }
    }
}
