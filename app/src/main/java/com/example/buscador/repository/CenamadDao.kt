package com.example.buscador.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.buscador.model.InvestigadorEntity
import com.example.buscador.model.LineaInvestigacionEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO del caso CENAMAD: única puerta de entrada a las tablas
 * "lineas_investigacion" e "investigadores".
 *
 * Separado de [UsuarioDao] a propósito: cada dominio (perfiles del usuario vs.
 * investigación del centro) evoluciona por su cuenta y evita inflar una interfaz
 * con responsabilidades ajenas.
 *
 * Las consultas de lectura exponen [Flow] para que Room notifique automáticamente
 * a los observers cada vez que cambie una fila (flujo reactivo unidireccional,
 * mismo criterio que el resto de la base). Las escrituras son `suspend` y Room
 * las ejecuta fuera del hilo principal.
 */
@Dao
interface CenamadDao {

    /**
     * Observa todas las líneas de investigación ordenadas por título.
     * El Flow reemite solo cuando un INSERT/UPDATE toca la tabla.
     */
    @Query("SELECT * FROM lineas_investigacion ORDER BY titulo ASC")
    fun observarTodasLasLineas(): Flow<List<LineaInvestigacionEntity>>

    /**
     * Observa los investigadores de una línea concreta, ordenados alfabéticamente.
     * Usa el índice declarado sobre `linea_id` en [InvestigadorEntity].
     */
    @Query("SELECT * FROM investigadores WHERE linea_id = :lineaId ORDER BY nombre ASC")
    fun observarInvestigadoresPorLinea(lineaId: Long): Flow<List<InvestigadorEntity>>

    /**
     * Inserta una línea de investigación y devuelve el id autogenerado.
     * ABORT es coherente con [UsuarioDao]: nunca se sobrescriben filas en silencio.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarLinea(linea: LineaInvestigacionEntity): Long

    /** Inserta un investigador asociado a su línea y devuelve el id autogenerado. */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarInvestigador(investigador: InvestigadorEntity): Long
}
