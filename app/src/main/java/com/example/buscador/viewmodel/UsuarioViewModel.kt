package com.example.buscador.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buscador.BuscadorApplication
import com.example.buscador.model.Usuario
import com.example.buscador.model.ValidadorPerfil
import com.example.buscador.repository.UsuarioRepository
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
 * Estado del formulario de registro de una cuenta nueva.
 *
 * Agrupa los tres campos con su mensaje de error reactivo: el ViewModel recalcula
 * los errores en cada pulsación de tecla, así la pantalla solo pinta lo que lee
 * aquí (la UI no aplica reglas de validación propias).
 *
 * @property nombre Texto capturado en el campo Nombre.
 * @property correo Texto capturado en el campo Correo.
 * @property contrasena Texto capturado en el campo Contraseña.
 * @property errorNombre Mensaje reactivo del nombre; null si el campo está aceptable.
 * @property errorCorreo Mensaje reactivo del correo; null si el campo está aceptable.
 * @property errorContrasena Mensaje reactivo de la contraseña; null si es aceptable.
 * @property estaRegistrando `true` mientras la inserción en Room está en curso.
 * @property correoRegistrado Correo normalizado cuando Room confirmó el INSERT;
 *   la pantalla lo observa para cerrar el formulario y navegar.
 * @property mensajeError Error general del intento de registro (correo duplicado,
 *   fallo de escritura, etc.); null si no hay errores pendientes.
 */
data class EstadoRegistro(
    val nombre: String = "",
    val correo: String = "",
    val contrasena: String = "",
    val errorNombre: String? = null,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val estaRegistrando: Boolean = false,
    val correoRegistrado: String? = null,
    val mensajeError: String? = null
) {
    /** El botón "Crear cuenta" solo se habilita con los tres campos sin errores. */
    val formularioEsValido: Boolean
        get() = nombre.isNotBlank() &&
            errorNombre == null &&
            correo.isNotBlank() && errorCorreo == null &&
            contrasena.isNotBlank() && errorContrasena == null
}

/**
 * ViewModel del perfil y de la autenticación del usuario.
 *
 * Responsabilidades (MVVM):
 * - Exponer el estado del usuario como [StateFlow] inmutable para la UI.
 * - Autenticar el ingreso contra la tabla "usuarios" de Room: ya no existen
 *   credenciales estáticas; el correo y la contraseña deben corresponder a un
 *   registro real creado en el formulario de registro.
 * - Mantener la sesión identificada por correo ([correoUsuarioAutenticado]): con
 *   varios usuarios en la base, "el primero de la tabla" deja de ser una definición
 *   válida de "usuario activo"; el perfil observado es siempre la fila del correo.
 * - Orquestar las escrituras en el repositorio dentro de [viewModelScope],
 *   de modo que se cancelen automáticamente si la pantalla muere.
 * - Traducir fallos de Room (por ejemplo, correo duplicado) a mensajes de UI.
 * - No conoce Compose ni Room: solo el contrato [UsuarioRepository].
 */
@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModel(
    private val repositorioUsuario: UsuarioRepository
) : ViewModel() {

    /**
     * Correo normalizado del usuario autenticado; `null` equivale a sin sesión
     * (invitado o cierre de sesión). Es la única fuente de identidad de la sesión:
     * no se guardan credenciales ni perfiles en memoria.
     */
    private val _correoUsuarioAutenticado = MutableStateFlow<String?>(null)

    /**
     * Usuario activo observado desde Room según el correo de la sesión.
     * `flatMapLatest` reemplaza la consulta cada vez que cambia la identidad:
     * al iniciar sesión se suscribe a la fila de ese correo y, al cerrarla,
     * emite `null` sin borrar nada de la base.
     */
    val usuarioActual: StateFlow<Usuario?> = _correoUsuarioAutenticado
        .flatMapLatest { correoElectronico ->
            if (correoElectronico == null) {
                flowOf(null)
            } else {
                repositorioUsuario.observarUsuarioPorCorreo(correoElectronico)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_SUSCRIPCION_MS),
            initialValue = null
        )

    /**
     * `true` mientras Room no ha entregado la primera lectura.
     * Permite distinguir "todavía no sé si hay perfil" de "el usuario no tiene perfil",
     * y así evitar mostrar "sin configurar" durante el primer frame.
     */
    val estaCargandoPerfil: StateFlow<Boolean> = usuarioActual
        .map { false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_SUSCRIPCION_MS),
            initialValue = true
        )

    /**
     * Bandera del último intento de login rechazado.
     * [validarCredenciales] la enciende cuando el correo no existe en Room o la
     * contraseña no coincide, y la UI la usa para marcar los campos del formulario
     * en rojo. Se apaga al corregir los datos o al acertar.
     */
    private val _credencialesIncorrectas = MutableStateFlow(false)

    /** La pantalla de login solo lee este estado para pintar el error visual. */
    val credencialesIncorrectas: StateFlow<Boolean> = _credencialesIncorrectas.asStateFlow()

    /** Estado del formulario de edición; la UI solo lo lee. */
    private val _estadoEdicion = MutableStateFlow<EstadoEdicionPerfil>(
        EstadoEdicionPerfil.Inactivo
    )
    val estadoEdicion: StateFlow<EstadoEdicionPerfil> = _estadoEdicion.asStateFlow()

    /** Estado del formulario de registro; la UI solo lo lee. */
    private val _estadoRegistro = MutableStateFlow(EstadoRegistro())
    val estadoRegistro: StateFlow<EstadoRegistro> = _estadoRegistro.asStateFlow()

    /**
     * Evita escrituras concurrentes.
     * No se usa [estadoEdicion] como cerrojo porque su valor pertenece a la UI y
     * puede reiniciarse al salir y volver a entrar a la pantalla.
     */
    private var escrituraEnCurso = false

    /**
     * Persiste el perfil completo del usuario autenticado (UPDATE sobre su fila).
     *
     * La fila se resuelve por el correo de la sesión, no por "el primer registro"
     * de la tabla: con múltiples usuarios solo debe modificarse el perfil propio.
     * El `copy` conserva la contraseña y demás columnas que el formulario de
     * edición no muestra; la UI nunca envía la contraseña por este camino.
     * Si no hay sesión (caso raro de un perfil heredado sin correo registrado),
     * se inserta una fila nueva con contraseña vacía y la sesión la adopta.
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
                val correoNormalizado = correoElectronico.trim().lowercase(Locale.ROOT)
                val registroExistente = _correoUsuarioAutenticado.value
                    ?.let { repositorioUsuario.buscarUsuarioPorCorreo(it) }

                val usuarioAGuardar = if (registroExistente != null) {
                    // UPDATE: se conserva la contraseña guardada fuera de este formulario.
                    registroExistente.copy(
                        nombreCompleto = nombreCompleto.trim(),
                        correoElectronico = correoNormalizado,
                        biografia = biografia.trim(),
                        uriFotoPerfil = uriFotoPerfil
                    )
                } else {
                    Usuario(
                        nombreCompleto = nombreCompleto.trim(),
                        correoElectronico = correoNormalizado,
                        biografia = biografia.trim(),
                        uriFotoPerfil = uriFotoPerfil,
                        contrasena = ""
                    )
                }
                repositorioUsuario.guardarUsuario(usuarioAGuardar)
                _estadoEdicion.value = EstadoEdicionPerfil.Guardado
                // La sesión sigue al correo definitivo del perfil guardado: si el
                // usuario corrigió su correo, "Mi perfil" reobserva la fila nueva.
                _correoUsuarioAutenticado.value = correoNormalizado
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
     * Autentica un intento de ingreso contra la tabla "usuarios" de Room.
     *
     * Es `suspend` porque la consulta al DAO debe salir del hilo principal; quien
     * la invoca (el NavHost) lanza una corutina y navega solo con resultado `true`.
     * El correo se compara normalizado (trim + minúsculas), igual que se escribe en
     * el registro; la contraseña se compara exacta tras el trim. Un perfil sin
     * contraseña (creado como invitado) no admite ingreso: debe registrar su cuenta.
     *
     * Al fallar enciende [_credencialesIncorrectas] para que la pantalla marque
     * los campos; al acertar apaga cualquier error previo.
     *
     * @return `true` si existe un usuario real con esas credenciales.
     */
    suspend fun validarCredenciales(correoElectronico: String, contrasena: String): Boolean {
        val correoNormalizado = correoElectronico.trim().lowercase(Locale.ROOT)
        val usuarioRegistrado = if (correoNormalizado.isBlank()) {
            null
        } else {
            repositorioUsuario.buscarUsuarioPorCorreo(correoNormalizado)
        }
        val ingresoValido = usuarioRegistrado != null &&
            usuarioRegistrado.contrasena.isNotBlank() &&
            usuarioRegistrado.contrasena == contrasena.trim()
        _credencialesIncorrectas.value = !ingresoValido
        return ingresoValido
    }

    /** Apaga el error de credenciales; se invoca al editar los campos del login. */
    fun limpiarErrorCredenciales() {
        _credencialesIncorrectas.value = false
    }

    /**
     * Abre la sesión del usuario recién autenticado.
     *
     * Con la autenticación real ya no se "siembra" ningún perfil: la fila existe
     * porque el usuario pasó por el formulario de registro. Aquí solo se fija el
     * correo de sesión; [usuarioActual] comienza a observar esa fila en Room y
     * "Mi perfil" muestra los datos reales sin escritura adicional.
     */
    fun registrarIngreso(correoElectronico: String) {
        val correoNormalizado = correoElectronico.trim().lowercase(Locale.ROOT)
        if (!ValidadorPerfil.esCorreoValido(correoNormalizado)) return
        _correoUsuarioAutenticado.value = correoNormalizado
    }

    /**
     * Prepara el modo invitado antes de navegar al Home.
     *
     * Dejar el correo de sesión en `null` hace que [usuarioActual] emita `null`
     * en memoria sin borrar filas de Room: "Mi perfil" muestra su estado vacío
     * aunque existan perfiles persistidos de ingresos anteriores.
     */
    fun ingresarComoInvitado() {
        _correoUsuarioAutenticado.value = null
    }

    /**
     * Termina la sesión actual.
     *
     * El perfil persistido no se borra de Room: solo deja de exponerse a la UI
     * hasta el próximo [registrarIngreso], de modo que volver a ingresar con el
     * mismo correo y contraseña recupera los datos tal como quedaron.
     */
    fun cerrarSesion() {
        _correoUsuarioAutenticado.value = null
    }

    /**
     * Devuelve el formulario de edición a su estado base.
     * Se invoca al salir de la pantalla de edición para que un "Guardado" anterior
     * no vuelva a disparar la navegación al reingresar.
     */
    fun finalizarEdicion() {
        _estadoEdicion.value = EstadoEdicionPerfil.Inactivo
    }

    /**
     * Actualiza el campo Nombre del registro: en vivo solo se apaga el error de
     * obligatoriedad cuando el usuario empieza a escribir; la regla se comprueba
     * de nuevo al enviar ([registrarUsuario]).
     */
    fun actualizarNombreRegistro(nuevoNombre: String) {
        _estadoRegistro.value = _estadoRegistro.value.copy(
            nombre = nuevoNombre,
            errorNombre = if (nuevoNombre.isBlank()) _estadoRegistro.value.errorNombre else null,
            mensajeError = null
        )
    }

    /**
     * Actualiza el campo Correo con validación reactiva: mientras el usuario escribe,
     * un formato inválido pinta [MENSAJE_CORREO_INVALIDO] en el propio campo; un
     * campo vacío no molesta todavía (el error aparece al intentar registrarse).
     */
    fun actualizarCorreoRegistro(nuevoCorreo: String) {
        val errorReactivo = when {
            nuevoCorreo.isBlank() -> null
            !ValidadorPerfil.esCorreoValido(nuevoCorreo) -> MENSAJE_CORREO_INVALIDO
            else -> null
        }
        _estadoRegistro.value = _estadoRegistro.value.copy(
            correo = nuevoCorreo,
            errorCorreo = errorReactivo,
            mensajeError = null
        )
    }

    /**
     * Actualiza el campo Contraseña con validación reactiva por longitud mínima
     * ([ValidadorPerfil.LONGITUD_MINIMA_CONTRASENA]); igual que el correo, un campo
     * vacío no muestra error hasta el intento de registro.
     */
    fun actualizarContrasenaRegistro(nuevaContrasena: String) {
        val errorReactivo = when {
            nuevaContrasena.isBlank() -> null
            !ValidadorPerfil.esContrasenaValida(nuevaContrasena) -> MENSAJE_CONTRASENA_CORTA
            else -> null
        }
        _estadoRegistro.value = _estadoRegistro.value.copy(
            contrasena = nuevaContrasena,
            errorContrasena = errorReactivo,
            mensajeError = null
        )
    }

    /**
     * Registra una cuenta nueva en Room.
     *
     * Vuelve a validar los tres campos (defensa en profundidad aunque la pantalla
     * ya los pinte) y, si todo es correcto, inserta el usuario con el correo
     * normalizado y la contraseña tal cual se captura. Un correo ya registrado
     * choca contra el índice único de la tabla y se traduce a [MENSAJE_CORREO_DUPLICADO].
     * Al confirmar la escritura, [EstadoRegistro.correoRegistrado] deja de ser null
     * y la pantalla usa ese valor para navegar (nadie navega "a ciegas").
     */
    fun registrarUsuario() {
        val estadoActual = _estadoRegistro.value
        val errorNombre = when {
            !ValidadorPerfil.esNombreValido(estadoActual.nombre) -> MENSAJE_NOMBRE_OBLIGATORIO
            else -> null
        }
        val errorCorreo = when {
            estadoActual.correo.isBlank() -> MENSAJE_CORREO_OBLIGATORIO
            !ValidadorPerfil.esCorreoValido(estadoActual.correo) -> MENSAJE_CORREO_INVALIDO
            else -> null
        }
        val errorContrasena = when {
            estadoActual.contrasena.isBlank() -> MENSAJE_CONTRASENA_OBLIGATORIA
            !ValidadorPerfil.esContrasenaValida(estadoActual.contrasena) -> MENSAJE_CONTRASENA_CORTA
            else -> null
        }

        _estadoRegistro.value = estadoActual.copy(
            errorNombre = errorNombre,
            errorCorreo = errorCorreo,
            errorContrasena = errorContrasena
        )
        if (errorNombre != null || errorCorreo != null || errorContrasena != null) return
        if (estadoActual.estaRegistrando) return

        viewModelScope.launch {
            _estadoRegistro.value = _estadoRegistro.value.copy(
                estaRegistrando = true,
                mensajeError = null
            )
            try {
                val correoNormalizado = estadoActual.correo.trim().lowercase(Locale.ROOT)
                repositorioUsuario.guardarUsuario(
                    Usuario(
                        nombreCompleto = estadoActual.nombre.trim(),
                        correoElectronico = correoNormalizado,
                        biografia = "",
                        contrasena = estadoActual.contrasena.trim()
                    )
                )
                _estadoRegistro.value = _estadoRegistro.value.copy(
                    estaRegistrando = false,
                    correoRegistrado = correoNormalizado
                )
            } catch (cancelacion: CancellationException) {
                throw cancelacion
            } catch (error: Exception) {
                _estadoRegistro.value = _estadoRegistro.value.copy(
                    estaRegistrando = false,
                    mensajeError = when (error) {
                        is SQLiteConstraintException -> MENSAJE_CORREO_DUPLICADO
                        else -> MENSAJE_ERROR_REGISTRO_GENERICO
                    }
                )
            }
        }
    }

    /**
     * Devuelve el formulario de registro a su estado base.
     * Se invoca al desmontar la pantalla para que un "correoRegistrado" anterior
     * no vuelva a disparar la navegación al reingresar al formulario.
     */
    fun limpiarEstadoRegistro() {
        _estadoRegistro.value = EstadoRegistro()
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
        private const val MENSAJE_CORREO_OBLIGATORIO =
            "El correo electrónico es obligatorio."
        private const val MENSAJE_CORREO_INVALIDO =
            "Ingresa un correo electrónico válido (ejemplo: nombre@cenamad.cl)."
        private const val MENSAJE_CORREO_DUPLICADO =
            "Ese correo ya está registrado en otro perfil."
        private const val MENSAJE_CONTRASENA_OBLIGATORIA =
            "La contraseña es obligatoria."
        private const val MENSAJE_CONTRASENA_CORTA =
            "La contraseña debe tener al menos " +
                "${ValidadorPerfil.LONGITUD_MINIMA_CONTRASENA} caracteres."
        private const val MENSAJE_ERROR_GENERICO =
            "No se pudo guardar el perfil. Inténtalo nuevamente."
        private const val MENSAJE_ERROR_REGISTRO_GENERICO =
            "No se pudo crear la cuenta. Inténtalo nuevamente."

        /**
         * Factory manual: obtiene el repositorio desde el contenedor de la Application
         * y lo inyecta en el constructor (sin librerías de DI).
         */
        val Fabrica: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BuscadorApplication
                UsuarioViewModel(
                    repositorioUsuario = aplicacion.contenedorAplicacion.repositorioUsuario
                )
            }
        }
    }
}
