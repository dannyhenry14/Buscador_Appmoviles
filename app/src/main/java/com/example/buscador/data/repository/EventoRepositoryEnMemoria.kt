package com.example.buscador.data.repository

import com.example.buscador.R
import com.example.buscador.model.Evento

/**
 * Repositorio en memoria con datos de ejemplo de CENAMAD.
 *
 * Es una implementación temporal de [EventoRepository] que permite desarrollar
 * las pantallas de Eventos y DetalleEvento sin backend. Al no tener estado mutable
 * se puede exponer como `object` seguro entre pantallas.
 *
 * TODO: reemplazar por una fuente Room o una API remota cuando el backend esté definido.
 */
class EventoRepositoryEnMemoria : EventoRepository {

    /** Catálogo de ejemplo alineado con las actividades de difusión de CENAMAD. */
    private val catalogoEventos: List<Evento> = listOf(
        Evento(
            idEvento = 1L,
            titulo = "Seminario de Construcción en Madera",
            descripcion = "Jornada técnica sobre sistemas constructivos de mediana altura, " +
                "con casos reales y revisión de normativa vigente.",
            fecha = "12 de noviembre de 2026",
            ubicacion = "Auditorio CENAMAD, Santiago",
            imagenResId = R.drawable.img_seminario
        ),
        Evento(
            idEvento = 2L,
            titulo = "Taller de Diseño Estructural",
            descripcion = "Taller práctico de dimensionamiento de elementos de madera " +
                "aplicando la norma NCh1198 y herramientas de cálculo.",
            fecha = "26 de noviembre de 2026",
            ubicacion = "Laboratorio de Estructuras, Santiago",
            imagenResId = R.drawable.img_taller
        ),
        Evento(
            idEvento = 3L,
            titulo = "Feria de Innovación Forestal",
            descripcion = "Exposición de productos de ingeniería de madera, proveedores " +
                "y proyectos de investigación aplicada del centro.",
            fecha = "10 de diciembre de 2026",
            ubicacion = "Parque de Innovación, Concepción",
            imagenResId = R.drawable.img_feria
        ),
        Evento(
            idEvento = 4L,
            titulo = "Charla: Normativa y Certificación",
            descripcion = "Actualización sobre certificación de madera estructural y " +
                "requisitos para proyectos de construcción sustentable.",
            fecha = "15 de enero de 2027",
            ubicacion = "Modalidad híbrida",
            imagenResId = R.drawable.img_charla
        )
    )

    override fun obtenerTodosLosEventos(): List<Evento> = catalogoEventos

    override fun obtenerEventoPorId(idEvento: Long): Evento? =
        catalogoEventos.firstOrNull { evento -> evento.idEvento == idEvento }
}
