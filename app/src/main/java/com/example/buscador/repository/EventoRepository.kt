package com.example.buscador.repository

import com.example.buscador.model.Evento
import com.example.buscador.model.CategoriaEvento

/**
 * Contrato de acceso a los eventos de CENAMAD.
 *
 * Se define como interfaz para que el ViewModel no dependa de la fuente concreta.
 * Cuando exista backend o una tabla Room de eventos, basta con crear otra
 * implementación y cambiarla en el ContenedorAplicacion.
 */
interface EventoRepository {

    /** Devuelve el listado completo de eventos disponibles. */
    fun obtenerTodosLosEventos(): List<Evento>

    /** Busca un evento por su identificador; null si no existe. */
    fun obtenerEventoPorId(idEvento: Long): Evento?


    /**
     * Busca eventos aplicando los filtros recibidos.
     * Un filtro en null significa "no filtrar por ese criterio".
     *
     * @param texto Palabra a buscar en el título o la descripción; vacío no filtra.
     * @param categoria Interés elegido por el usuario; null muestra todas.
     * @param fechaDesdeMillis Fecha mínima del evento (inclusive); null no pone límite.
     * @param fechaHastaMillis Fecha máxima del evento (inclusive); null no pone límite.
     */
    fun buscarEventos(
        texto: String,
        categoria: CategoriaEvento?,
        fechaDesdeMillis: Long?,
        fechaHastaMillis: Long?
    ): List<Evento>
}
