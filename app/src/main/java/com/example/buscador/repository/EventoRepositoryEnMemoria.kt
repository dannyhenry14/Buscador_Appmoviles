package com.example.buscador.repository

import com.example.buscador.R
import com.example.buscador.model.CategoriaEvento
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
            fechaMillis = 1794441600000L,
            ubicacion = "Auditorio CENAMAD, Santiago",
            categoria = CategoriaEvento.SEMINARIO,
            imagenResId = R.drawable.img_seminario
        ),
        Evento(
            idEvento = 2L,
            titulo = "Taller de Diseño Estructural",
            descripcion = "Taller práctico de dimensionamiento de elementos de madera " +
                "aplicando la norma NCh1198 y herramientas de cálculo.",
            fecha = "26 de noviembre de 2026",
            fechaMillis = 1795651200000L,
            ubicacion = "Laboratorio de Estructuras, Santiago",
            categoria = CategoriaEvento.TALLER,
            imagenResId = R.drawable.img_taller
        ),
        Evento(
            idEvento = 3L,
            titulo = "Feria de Innovación Forestal",
            descripcion = "Exposición de productos de ingeniería de madera, proveedores " +
                "y proyectos de investigación aplicada del centro.",
            fecha = "10 de diciembre de 2026",
            fechaMillis = 1796860800000L,
            ubicacion = "Parque de Innovación, Concepción",
            categoria = CategoriaEvento.FERIA,
            imagenResId = R.drawable.img_feria
        ),
        Evento(
            idEvento = 4L,
            titulo = "Charla: Normativa y Certificación",
            descripcion = "Actualización sobre certificación de madera estructural y " +
                "requisitos para proyectos de construcción sustentable.",
            fecha = "15 de enero de 2027",
            fechaMillis = 1799971200000L,
            ubicacion = "Modalidad híbrida",
            categoria = CategoriaEvento.CHARLA,
            imagenResId = R.drawable.img_charla
        )
    )

    override fun obtenerTodosLosEventos(): List<Evento> = catalogoEventos

    override fun obtenerEventoPorId(idEvento: Long): Evento? =
        catalogoEventos.firstOrNull { evento -> evento.idEvento == idEvento }

    override fun buscarEventos(
        texto: String,
        categoria: CategoriaEvento?,
        fechaDesdeMillis: Long?,
        fechaHastaMillis: Long?
    ): List<Evento> {
        val textoLimpio = texto.trim()
        return catalogoEventos.filter { evento ->
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
