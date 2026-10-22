package com.example.buscador.repository

import com.example.buscador.model.Evento

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
}
