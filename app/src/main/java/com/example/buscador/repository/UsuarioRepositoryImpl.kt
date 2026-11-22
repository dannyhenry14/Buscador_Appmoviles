package com.example.buscador.repository

import com.example.buscador.model.Usuario
import com.example.buscador.model.aEntidadRoom
import com.example.buscador.model.aModeloDominio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.example.buscador.remote.CenamadApi
import com.example.buscador.remote.aModeloDominio
import com.example.buscador.remote.aRequestDto

/**
 * Implementación concreta de [UsuarioRepository] respaldada por Xano y Room.
 *
 * Responsabilidades:
 * - Traducir entidades de Room/API a modelos de dominio y viceversa.
 * - Decidir si una escritura es INSERT o UPDATE (según el id del usuario).
 * - Delegar en [AlmacenSesion] el guardado de la sesión abierta.
 * No contiene lógica de UI ni de estado: eso pertenece al ViewModel.
 */
class UsuarioRepositoryImpl(
    private val api: CenamadApi,
    private val daoUsuario: UsuarioDao,
    private val almacenSesion: AlmacenSesion
) : UsuarioRepository {

    override fun observarUsuarioPrincipal(): Flow<Usuario?> =
        daoUsuario.observarUsuarioPrincipal().map { entidadUsuario ->
            entidadUsuario?.aModeloDominio()
        }

    override fun observarTodosLosUsuarios(): Flow<List<Usuario>> =
        daoUsuario.observarTodosLosUsuarios().map { listaEntidades ->
            listaEntidades.map { entidadUsuario -> entidadUsuario.aModeloDominio() }
        }

    override fun observarUsuarioPorCorreo(correoElectronico: String): Flow<Usuario?> =
        daoUsuario.observarUsuarioPorCorreo(correoElectronico).map { entidadUsuario ->
            entidadUsuario?.aModeloDominio()
        }

    override suspend fun buscarUsuarioPorCorreo(correoElectronico: String): Usuario? =
        daoUsuario.buscarUsuarioPorCorreo(correoElectronico)?.aModeloDominio()

    override suspend fun refrescarUsuarioPorId(id: Long): Result<Usuario> {
        return try {
            val remoto = api.obtenerUsuarioPorId(id).aModeloDominio()
            daoUsuario.actualizarUsuario(remoto.aEntidadRoom())
            Result.success(remoto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun guardarUsuario(usuario: Usuario): Long {
        return if (usuario.idUsuario == Usuario.ID_SIN_ASIGNAR) {
            val remoto = api.crearUsuario(usuario.aRequestDto()).aModeloDominio()
            val id = daoUsuario.insertarUsuario(remoto.aEntidadRoom())
            almacenSesion.guardarCorreoSesion(remoto.correoElectronico)
            id
        } else {
            val remoto = api.actualizarUsuario(usuario.idUsuario.toInt(), usuario.aRequestDto()).aModeloDominio()
            daoUsuario.actualizarUsuario(remoto.aEntidadRoom())
            remoto.idUsuario
        }
    }

    override suspend fun iniciarSesion(correoElectronico: String, contrasena: String): Usuario? {
        try {
            val remotos = api.obtenerUsuarios()
            val usuarioEncontrado = remotos.find {
                it.correoElectronico.equals(correoElectronico, ignoreCase = true) && it.contrasena == contrasena
            }?.aModeloDominio()
            
            if (usuarioEncontrado != null) {
                val existeLocal = daoUsuario.buscarUsuarioPorCorreo(correoElectronico)
                if (existeLocal != null) {
                    daoUsuario.actualizarUsuario(usuarioEncontrado.aEntidadRoom())
                } else {
                    daoUsuario.insertarUsuario(usuarioEncontrado.aEntidadRoom())
                }
                almacenSesion.guardarCorreoSesion(usuarioEncontrado.correoElectronico)
                return usuarioEncontrado
            }
            return null
        } catch (e: Exception) {
            val local = daoUsuario.buscarUsuarioPorCorreo(correoElectronico)?.aModeloDominio()
            if (local != null && local.contrasena == contrasena) {
                almacenSesion.guardarCorreoSesion(local.correoElectronico)
                return local
            }
            throw e
        }
    }

    override suspend fun actualizarFotoPerfil(idUsuario: Long, uriFotoPerfil: String?) {
        daoUsuario.actualizarUriFotoPerfil(idUsuario = idUsuario, uriFotoPerfil = uriFotoPerfil)
    }

    override suspend fun eliminarCuenta(id: Long): Result<Unit> {
        return try {
            val response = api.eliminarUsuario(id)
            if (response.isSuccessful || response.code() == 404) {
                // Borramos en local y limpiamos sesión
                val usuarioLocal = daoUsuario.buscarUsuarioPorCorreo(almacenSesion.leerCorreoSesion() ?: "")
                if (usuarioLocal != null && usuarioLocal.idUsuario == id) {
                    daoUsuario.eliminarUsuario(usuarioLocal)
                }
                almacenSesion.borrarSesion()
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error en API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun eliminarUsuario(usuario: Usuario) {
        daoUsuario.eliminarUsuario(usuario.aEntidadRoom())
    }

    override fun obtenerCorreoSesionGuardada(): String? = almacenSesion.leerCorreoSesion()

    override fun guardarCorreoSesion(correoElectronico: String) {
        almacenSesion.guardarCorreoSesion(correoElectronico)
    }

    override fun borrarSesionGuardada() {
        almacenSesion.borrarSesion()
    }
}