package com.example.buscador.model

/**
 * Modelo de dominio de un evento o actividad organizada por CENAMAD.
 *
 * Por ahora la información proviene de una fuente en memoria (ver
 * data/repository/EventoRepository.kt); el modelo ya está preparado para
 * ser respaldado por Room o por una API remota en una etapa posterior.
 *
 * @property idEvento Identificador del evento, usado como argumento de navegación.
 * @property titulo Nombre visible del evento en el listado y en el detalle.
 * @property descripcion Resumen o programa de la actividad.
 * @property fecha Fecha legible para el usuario (formato presentacional).
 * @property ubicacion Lugar físico o modalidad donde se realiza el evento.
 */
data class Evento(
    val idEvento: Long,
    val titulo: String,
    val descripcion: String,
    val fecha: String,
    val ubicacion: String
)
