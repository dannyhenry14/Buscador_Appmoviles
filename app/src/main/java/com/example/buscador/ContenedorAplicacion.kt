package com.example.buscador

import android.content.Context
import com.example.buscador.repository.AlmacenFotosPerfil
import com.example.buscador.repository.BuscadorDatabase
import com.example.buscador.repository.EventoRepository
import com.example.buscador.repository.EventoRepositoryEnMemoria
import com.example.buscador.repository.UsuarioRepository
import com.example.buscador.repository.UsuarioRepositoryImpl

/**
 * Contenedor de dependencias manual (sin Hilt para mantener el proyecto simple).
 *
 * Se instancia una sola vez en [BuscadorApplication] y expone
 * repositorios listos para inyectar en los ViewModel mediante sus factories.
 * Aquí se decide qué implementación concreta se usa para cada contrato.
 */
class ContenedorAplicacion(contexto: Context) {

    /** Instancia única de Room; crear la base de datos es costoso, no debe repetirse. */
    private val baseDeDatos: BuscadorDatabase = BuscadorDatabase.obtenerInstancia(contexto)

    /** Repositorio de usuarios respaldado por Room. */
    val repositorioUsuario: UsuarioRepository = UsuarioRepositoryImpl(
        daoUsuario = baseDeDatos.usuarioDao()
    )

    /** Repositorio de eventos; por ahora en memoria, intercambiable por Room/API. */
    val repositorioEvento: EventoRepository = EventoRepositoryEnMemoria()

    /** Almacén de fotos de perfil; instancia única porque solo retiene el contexto. */
    val almacenFotosPerfil: AlmacenFotosPerfil = AlmacenFotosPerfil(contexto)
}
