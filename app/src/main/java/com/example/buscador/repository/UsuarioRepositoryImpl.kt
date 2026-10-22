package com.example.buscador.repository

import com.example.buscador.model.Usuario
import com.example.buscador.model.aEntidadRoom
import com.example.buscador.model.aModeloDominio
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementación concreta de [UsuarioRepository] respaldada por Room.
 *
 * Responsabilidades:
 * - Traducir entidades de Room a modelos de dominio y viceversa.
 * - Decidir si una escritura es INSERT o UPDATE (según el id del usuario).
 * No contiene lógica de UI ni de estado: eso pertenece al ViewModel.
 */
class UsuarioRepositoryImpl(
    private val daoUsuario: UsuarioDao
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

    override suspend fun guardarUsuario(usuario: Usuario): Long {
        // id 0 significa "aún no persistido": Room asigna la clave primaria.
        // Cualquier otro id corresponde a una fila existente que se actualiza.
        return if (usuario.idUsuario == Usuario.ID_SIN_ASIGNAR) {
            daoUsuario.insertarUsuario(usuario.aEntidadRoom())
        } else {
            daoUsuario.actualizarUsuario(usuario.aEntidadRoom())
            usuario.idUsuario
        }
    }

    override suspend fun actualizarFotoPerfil(idUsuario: Long, uriFotoPerfil: String?) {
        daoUsuario.actualizarUriFotoPerfil(idUsuario = idUsuario, uriFotoPerfil = uriFotoPerfil)
    }

    override suspend fun eliminarUsuario(usuario: Usuario) {
        daoUsuario.eliminarUsuario(usuario.aEntidadRoom())
    }
}
