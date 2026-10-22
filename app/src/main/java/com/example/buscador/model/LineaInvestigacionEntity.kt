package com.example.buscador.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de persistencia que representa la tabla "lineas_investigacion" de Room.
 *
 * Modela una línea de investigación del CENAMAD (caso de estudio del proyecto).
 * Al igual que [UsuarioEntity], se mantiene separada del modelo de dominio para
 * que la capa de presentación no dependa de anotaciones de base de datos.
 *
 * Decisiones técnicas:
 * - Las columnas usan snake_case mediante @ColumnInfo para mantener el esquema
 *   SQL consistente con la tabla "usuarios" ya existente.
 * - No se declara unicidad en el título: dos líneas pueden ser homónimas si difieren
 *   en área temática, así que la integridad queda a criterio del repositorio.
 *
 * @property id Clave primaria autogenerada.
 * @property titulo Nombre con el que se identifica la línea de investigación.
 * @property areaTematica Área del conocimiento a la que pertenece (ej. "Salud ocupacional").
 * @property descripcion Resumen del alcance y objetivos de la línea.
 */
@Entity(tableName = "lineas_investigacion")
data class LineaInvestigacionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_linea")
    val id: Long = 0L,

    @ColumnInfo(name = "titulo")
    val titulo: String,

    @ColumnInfo(name = "area_tematica")
    val areaTematica: String,

    @ColumnInfo(name = "descripcion")
    val descripcion: String
)
