package com.example.buscador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buscador.BuscadorApplication
import com.example.buscador.data.repository.UsuarioRepository
import com.example.buscador.model.Usuario
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel del perfil del usuario.
 *
 * Responsabilidades (MVVM):
 * - Exponer el estado del usuario como [StateFlow] inmutable para la UI.
 * - Orquestar las escrituras en el repositorio dentro de [viewModelScope],
 *   de modo que se cancelen automáticamente si la pantalla muere.
 * - No conoce Compose ni Room: solo el contrato [UsuarioRepository].
 */
class UsuarioViewModel(
    private val repositorioUsuario: UsuarioRepository
) : ViewModel() {

    /**
     * Usuario activo observado desde Room.
     * `WhileSubscribed` corta la consulta 5 s después de que la UI deja de observar,
     * evitando trabajo innecesario cuando la app pasa a segundo plano.
     */
    val usuarioActual: StateFlow<Usuario?> = repositorioUsuario
        .observarUsuarioPrincipal()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_SUSCRIPCION_MS),
            initialValue = null
        )

    /**
     * Persiste el perfil completo.
     * Si todavía no existe un usuario en la base, el id llega como 0 y Room hace INSERT;
     * si ya existe, se conserva el id y se ejecuta UPDATE.
     */
    fun guardarPerfil(
        nombreCompleto: String,
        correoElectronico: String,
        biografia: String,
        uriFotoPerfil: String?
    ) {
        viewModelScope.launch {
            val usuarioExistente = usuarioActual.value
            val usuarioAPersistir = Usuario(
                idUsuario = usuarioExistente?.idUsuario ?: Usuario.ID_SIN_ASIGNAR,
                nombreCompleto = nombreCompleto.trim(),
                correoElectronico = correoElectronico.trim(),
                biografia = biografia.trim(),
                uriFotoPerfil = uriFotoPerfil
            )
            repositorioUsuario.guardarUsuario(usuarioAPersistir)
        }
    }

    /**
     * Actualiza solo la foto de perfil.
     * Se ignora si no hay usuario persistido: no se puede actualizar una fila inexistente.
     */
    fun actualizarFotoPerfil(uriFotoPerfil: String?) {
        val idUsuario = usuarioActual.value?.idUsuario ?: return
        viewModelScope.launch {
            repositorioUsuario.actualizarFotoPerfil(
                idUsuario = idUsuario,
                uriFotoPerfil = uriFotoPerfil
            )
        }
    }

    companion object {
        /** Tiempo de gracia antes de cancelar la observación de Room. */
        private const val TIEMPO_ESPERA_SUSCRIPCION_MS = 5_000L

        /**
         * Factory manual: obtiene el repositorio desde el contenedor de la Application
         * y lo inyecta en el constructor (sin librerías de DI).
         */
        val Fabrica: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BuscadorApplication
                UsuarioViewModel(aplicacion.contenedorAplicacion.repositorioUsuario)
            }
        }
    }
}
