package com.example.buscador.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.buscador.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de usuarios: única puerta de entrada a la tabla "usuarios".
 *
 * Las consultas de lectura exponen [Flow] para que Room notifique automáticamente
 * a los ViewModel cada vez que cambie una fila (flujo reactivo unidireccional).
 * Las operaciones de escritura son `suspend` y se ejecutan en un hilo de IO
 * gestionado por Room, nunca en el hilo principal.
 */
@Dao
interface UsuarioDao {

    /**
     * Observa al usuario activo de la app (primer registro por id).
     * Se usa un `LIMIT 1` en lugar de un id fijo para no depender de valores
     * mágicos y sobrevivir a inserciones/borrados durante el desarrollo.
     */
    @Query("SELECT * FROM usuarios ORDER BY id_usuario ASC LIMIT 1")
    fun observarUsuarioPrincipal(): Flow<UsuarioEntity?>

    /** Observa un usuario puntual; útil para futuras pantallas de administración. */
    @Query("SELECT * FROM usuarios WHERE id_usuario = :idUsuario LIMIT 1")
    fun observarUsuarioPorId(idUsuario: Long): Flow<UsuarioEntity?>

    /** Observa todos los usuarios registrados, ordenados alfabéticamente. */
    @Query("SELECT * FROM usuarios ORDER BY nombre_completo ASC")
    fun observarTodosLosUsuarios(): Flow<List<UsuarioEntity>>

    /** Búsqueda puntual por correo; base para una futura autenticación local. */
    @Query("SELECT * FROM usuarios WHERE correo_electronico = :correoElectronico LIMIT 1")
    suspend fun buscarUsuarioPorCorreo(correoElectronico: String): UsuarioEntity?

    /**
     * Inserta un usuario nuevo y devuelve el id autogenerado.
     * ABORT evita sobrescribir filas si el correo ya existe (índice único).
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    /** Actualiza todos los campos de un usuario existente (búsqueda por clave primaria). */
    @Update
    suspend fun actualizarUsuario(usuario: UsuarioEntity)

    /**
     * Actualización parcial: solo la URI de la foto.
     * Evita reescribir nombre/correo/biografía cuando el usuario únicamente cambia su imagen.
     */
    @Query("UPDATE usuarios SET uri_foto_perfil = :uriFotoPerfil WHERE id_usuario = :idUsuario")
    suspend fun actualizarUriFotoPerfil(idUsuario: Long, uriFotoPerfil: String?)

    /** Elimina un usuario; queda disponible para el cierre de sesión o limpieza de datos. */
    @Delete
    suspend fun eliminarUsuario(usuario: UsuarioEntity)
}
