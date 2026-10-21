package com.example.buscador.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buscador.BuscadorApplication
import com.example.buscador.data.repository.UsuarioRepository
import com.example.buscador.model.Usuario
import com.example.buscador.model.ValidadorPerfil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * Estado del formulario de edición del perfil.
 *
 * Se modela como jerarquía sellada para que la pantalla no pueda representar
 * combinaciones imposibles (por ejemplo, "guardando" y "error" a la vez).
 */
sealed interface EstadoEdicionPerfil {

    /** Formulario editable: no hay operaciones en curso ni errores pendientes. */
    data object Inactivo : EstadoEdicionPerfil

    /** Escritura en curso en Room; el botón se bloquea para evitar dobles INSERT/UPDATE. */
    data object Guardando : EstadoEdicionPerfil

    /** Escritura exitosa: la pantalla debe regresar al perfil ya actualizado. */
    data object Guardado : EstadoEdicionPerfil

    /** Escritura fallida: [mensaje] está listo para mostrarse al usuario. */
    data class Error(val mensaje: String) : EstadoEdicionPerfil
}

/**
 * ViewModel del perfil del usuario.
 *
 * Responsabilidades (MVVM):
 * - Exponer el estado del usuario como [StateFlow] inmutable para la UI.
 * - Orquestar las escrituras en el repositorio dentro de [viewModelScope],
 *   de modo que se cancelen automáticamente si la pantalla muere.
 * - Traducir fallos de Room (por ejemplo, correo duplicado) a mensajes de UI.
 * - No conoce Compose ni Room: solo el contrato [UsuarioRepository].
 */
class UsuarioViewModel(
    private val repositorioUsuario: UsuarioRepository
) : ViewModel() {

    /**
     * Flujo compartido con la consulta reactiva de Room.
     * Se crea una sola vez para que [usuarioActual] y [estaCargandoPerfil]
     * no dupliquen la consulta sobre la tabla de usuarios.
     */
    private val usuarioPersistido: StateFlow<Usuario?> = repositorioUsuario
        .observarUsuarioPrincipal()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_SUSCRIPCION_MS),
            initialValue = null
        )

    /**
     * Usuario activo observado desde Room.
     * `WhileSubscribed` corta la consulta 5 s después de que la UI deja de observar,
     * evitando trabajo innecesario cuando la app pasa a segundo plano.
     */
    val usuarioActual: StateFlow<Usuario?> = usuarioPersistido

    /**
     * `true` mientras Room no ha entregado la primera lectura.
     * Permite distinguir "todavía no sé si hay perfil" de "el usuario no tiene perfil",
     * y así evitar mostrar "sin configurar" durante el primer frame.
     */
    val estaCargandoPerfil: StateFlow<Boolean> = usuarioPersistido
        .map { false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_SUSCRIPCION_MS),
            initialValue = true
        )

    /** Estado del formulario de edición; la UI solo lo lee. */
    private val _estadoEdicion = MutableStateFlow<EstadoEdicionPerfil>(
        EstadoEdicionPerfil.Inactivo
    )
    val estadoEdicion: StateFlow<EstadoEdicionPerfil> = _estadoEdicion.asStateFlow()

    /**
     * Evita escrituras concurrentes.
     * No se usa [estadoEdicion] como cerrojo porque su valor pertenece a la UI y
     * puede reiniciarse al salir y volver a entrar a la pantalla.
     */
    private var escrituraEnCurso = false

    /**
     * Persiste el perfil completo (INSERT o UPDATE según exista previamente).
     *
     * El id se resuelve consultando Room en el momento de guardar, en lugar de leer
     * [usuarioActual].value: así el guardado no depende de que la UI esté observando
     * el perfil en ese instante.
     */
    fun guardarPerfil(
        nombreCompleto: String,
        correoElectronico: String,
        biografia: String,
        uriFotoPerfil: String?
    ) {
        // Ignora pulsaciones repetidas mientras hay una escritura en curso.
        if (escrituraEnCurso) return

        // Defensa en profundidad: la pantalla ya valida, pero el ViewModel no debe
        // permitir que datos inválidos lleguen a la base de datos.
        if (!ValidadorPerfil.esNombreValido(nombreCompleto)) {
            _estadoEdicion.value = EstadoEdicionPerfil.Error(MENSAJE_NOMBRE_OBLIGATORIO)
            return
        }
        if (!ValidadorPerfil.esCorreoValido(correoElectronico)) {
            _estadoEdicion.value = EstadoEdicionPerfil.Error(MENSAJE_CORREO_INVALIDO)
            return
        }

        escrituraEnCurso = true
        viewModelScope.launch {
            _estadoEdicion.value = EstadoEdicionPerfil.Guardando

            try {
                val usuarioPersistidoActual = repositorioUsuario
                    .observarUsuarioPrincipal()
                    .first()
                repositorioUsuario.guardarUsuario(
                    Usuario(
                        idUsuario = usuarioPersistidoActual?.idUsuario
                            ?: Usuario.ID_SIN_ASIGNAR,
                        nombreCompleto = nombreCompleto.trim(),
                        correoElectronico = correoElectronico.trim(),
                        biografia = biografia.trim(),
                        uriFotoPerfil = uriFotoPerfil
                    )
                )
                _estadoEdicion.value = EstadoEdicionPerfil.Guardado
            } catch (cancelacion: CancellationException) {
                // La pantalla se destruyó: se propaga la cancelación, no es un error de negocio.
                throw cancelacion
            } catch (error: Exception) {
                _estadoEdicion.value = EstadoEdicionPerfil.Error(mensajeDeError(error))
            } finally {
                escrituraEnCurso = false
            }
        }
    }

    /**
     * Materializa el perfil inicial cuando la base local está vacía.
     *
     * El login actual no autentica contra un servidor: solo captura un correo.
     * Para que "Mi perfil" tenga datos que leer desde el primer ingreso, se inserta
     * un perfil mínimo con ese correo. El usuario completará nombre y biografía
     * desde la pantalla de edición, que ejecutará un UPDATE sobre la misma fila.
     * Los invitados (correo vacío) no generan registro.
     */
    fun registrarIngreso(correoElectronico: String) {
        val correoNormalizado = correoElectronico.trim()
        if (!ValidadorPerfil.esCorreoValido(correoNormalizado)) return

        viewModelScope.launch {
            val perfilExistente = repositorioUsuario.observarUsuarioPrincipal().first()
            if (perfilExistente != null) return@launch

            runCatching {
                repositorioUsuario.guardarUsuario(
                    Usuario(
                        nombreCompleto = "",
                        correoElectronico = correoNormalizado,
                        biografia = ""
                    )
                )
            }
            // Un fallo aquí no debe bloquear el ingreso: el perfil todavía puede
            // crearse desde "Editar perfil" (la pantalla de login no espera el resultado).
        }
    }

    /**
     * Devuelve el formulario a su estado base.
     * Se invoca al salir de la pantalla de edición para que un "Guardado" anterior
     * no vuelva a disparar la navegación al reingresar.
     */
    fun finalizarEdicion() {
        _estadoEdicion.value = EstadoEdicionPerfil.Inactivo
    }

    /** Traduce excepciones técnicas a mensajes comprensibles para el usuario. */
    private fun mensajeDeError(error: Throwable): String = when (error) {
        // El índice único de correo impide duplicados; Room lanza al intentar escribirlo.
        is SQLiteConstraintException -> MENSAJE_CORREO_DUPLICADO
        else -> MENSAJE_ERROR_GENERICO
    }

    companion object {
        /** Tiempo de gracia antes de cancelar la observación de Room. */
        private const val TIEMPO_ESPERA_SUSCRIPCION_MS = 5_000L

        private const val MENSAJE_NOMBRE_OBLIGATORIO =
            "El nombre completo es obligatorio."
        private const val MENSAJE_CORREO_INVALIDO =
            "Ingresa un correo electrónico válido (ejemplo: nombre@cenamad.cl)."
        private const val MENSAJE_CORREO_DUPLICADO =
            "Ese correo ya está registrado en otro perfil."
        private const val MENSAJE_ERROR_GENERICO =
            "No se pudo guardar el perfil. Inténtalo nuevamente."

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
