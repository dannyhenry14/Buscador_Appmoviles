package com.example.buscador.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad de persistencia que representa la tabla "investigadores" de Room.
 *
 * Modela un investigador del CENAMAD asociado a una línea de investigación.
 *
 * Decisiones técnicas:
 * - `linea_id` se guarda como Long en lugar de una relación @ForeignKey: el caso
 *   de estudio no exige cascadas de borrado ni validación referencial de Room, y
 *   la asociación simple mantiene el esquema fácil de migrar.
 * - Se declara índice sobre `linea_id` porque la consulta dominante del DAO es
 *   "investigadores de una línea", que sin índice forzaría un escaneo completo.
 *
 * @property id Clave primaria autogenerada.
 * @property nombre Nombre y apellidos del investigador.
 * @property especialidad Disciplina o campo de expertise del investigador.
 * @property lineaId Referencia a [LineaInvestigacionEntity.id] a la que pertenece.
 */
@Entity(
    tableName = "investigadores",
    indices = [Index(value = ["linea_id"])]
)
data class InvestigadorEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id_investigador")
    val id: Long = 0L,

    @ColumnInfo(name = "nombre")
    val nombre: String,

    @ColumnInfo(name = "especialidad")
    val especialidad: String,

    @ColumnInfo(name = "linea_id")
    val lineaId: Long
)
