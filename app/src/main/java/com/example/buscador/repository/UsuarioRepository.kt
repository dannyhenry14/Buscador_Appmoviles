package com.example.buscador.repository

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

    /** Observa reactivamente al usuario de un correo; es la fila de la sesión activa. */
    fun observarUsuarioPorCorreo(correoElectronico: String): Flow<Usuario?>

    /** Busca un usuario por correo electrónico en local. */
    suspend fun buscarUsuarioPorCorreo(correoElectronico: String): Usuario?

    /** Refresca un usuario desde la API por su id y actualiza la base local. */
    suspend fun refrescarUsuarioPorId(id: Long): Result<Usuario>

    /** Autentica el ingreso de un usuario consultando la API remota (o local si falla). */
    suspend fun iniciarSesion(correoElectronico: String, contrasena: String): Usuario?

    /**
     * Inserta o actualiza un usuario según su id.
     * @return id del usuario persistido.
     */
    suspend fun guardarUsuario(usuario: Usuario): Long

    /** Actualiza únicamente la URI de la foto de perfil. */
    suspend fun actualizarFotoPerfil(idUsuario: Long, uriFotoPerfil: String?)

    /** Elimina un usuario de la base local y remota. */
    suspend fun eliminarCuenta(id: Long): Result<Unit>

    /** Elimina un usuario de la base local (obsoleto, prefiere eliminarCuenta). */
    suspend fun eliminarUsuario(usuario: Usuario)

    /** Devuelve el correo de la sesión guardada en el teléfono, o null si no hay sesión abierta. */
    fun obtenerCorreoSesionGuardada(): String?

    /** Guarda el correo del usuario que inició sesión, para que siga abierta al cerrar la app. */
    fun guardarCorreoSesion(correoElectronico: String)

    /** Borra la sesión guardada; se usa al cerrar sesión o al entrar como invitado. */
    fun borrarSesionGuardada()
}