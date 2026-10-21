package com.example.buscador.data.repository

import com.example.buscador.model.Usuario
import kotlinx.coroutines.flow.Flow

/**
 * Contrato de acceso a los datos de usuario.
 *
 * Los ViewModel dependen de esta abstracción y no del DAO ni de Room,
 * lo que permite sustituir la fuente de datos (por ejemplo, una API remota)
 * sin tocar la capa de presentación.
 */
interface UsuarioRepository {

    /** Observa al usuario activo de la app como modelo de dominio. */
    fun observarUsuarioPrincipal(): Flow<Usuario?>

    /** Observa todos los usuarios registrados. */
    fun observarTodosLosUsuarios(): Flow<List<Usuario>>

    /** Busca un usuario por correo electrónico. */
    suspend fun buscarUsuarioPorCorreo(correoElectronico: String): Usuario?

    /**
     * Inserta o actualiza un usuario según su id.
     * @return id del usuario persistido.
     */
    suspend fun guardarUsuario(usuario: Usuario): Long

    /** Actualiza únicamente la URI de la foto de perfil. */
    suspend fun actualizarFotoPerfil(idUsuario: Long, uriFotoPerfil: String?)

    /** Elimina un usuario de la base local. */
    suspend fun eliminarUsuario(usuario: Usuario)
}
