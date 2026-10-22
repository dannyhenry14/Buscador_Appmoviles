package com.example.buscador.viewmodel

import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buscador.BuscadorApplication
import com.example.buscador.data.local.almacenamiento.AlmacenFotosPerfil
import com.example.buscador.data.repository.UsuarioRepository
import com.example.buscador.model.Usuario
import com.example.buscador.model.ValidadorPerfil
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
 * - Autenticar el ingreso contra un usuario de prueba fijo (sin backend).
 * - No conoce Compose ni Room: solo el contrato [UsuarioRepository] y el
 *   almacén de fotos, para liberar archivos huérfanos tras sobrescribir un perfil.
 */
class UsuarioViewModel(
    private val repositorioUsuario: UsuarioRepository,
    private val almacenFotosPerfil: AlmacenFotosPerfil
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
     * Bandera de sesión activa.
     * El login la enciende y "Cerrar sesión" la apaga: mientras esté apagada, la
     * UI no observa el perfil aunque Room lo siga teniendo persistido.
     */
    private val _sesionActiva = MutableStateFlow(true)

    /**
     * Usuario activo observado desde Room, visible solo mientras la sesión está
     * abierta. Al cerrar sesión emite `null` sin borrar la fila: el próximo ingreso
     * con el mismo correo recupera el perfil tal como quedó guardado.
     * `WhileSubscribed` corta la consulta 5 s después de que la UI deja de observar,
     * evitando trabajo innecesario cuando la app pasa a segundo plano.
     */
    val usuarioActual: StateFlow<Usuario?> = combine(
        _sesionActiva,
        usuarioPersistido
    ) { sesionActiva, usuario ->
        if (sesionActiva) usuario else null
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(TIEMPO_ESPERA_SUSCRIPCION_MS),
        initialValue = null
    )

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

    /**
     * Bandera del último intento de login rechazado.
     * `validarCredenciales` la enciende cuando el par correo/contraseña no
     * coincide con el usuario de prueba y la UI la usa para marcar los campos
     * del formulario en rojo. Se apaga al corregir los datos o al acertar.
     */
    private val _credencialesIncorrectas = MutableStateFlow(false)

    /** La pantalla de login solo lee este estado para pintar el error visual. */
    val credencialesIncorrectas: StateFlow<Boolean> = _credencialesIncorrectas.asStateFlow()

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
                // Un invitado que registra su perfil pasa a tener sesión activa:
                // "Mi perfil" mostrará de inmediato el perfil recién creado.
                _sesionActiva.value = true
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
     * Autentica un intento de ingreso contra el usuario de prueba fijo.
     *
     * La app no tiene backend: las credenciales exigidas son las constantes
     * [CREDENCIAL_PRUEBA_CORREO] y [CREDENCIAL_PRUEBA_CONTRASENA] del companion.
     * El correo se compara sin distinguir mayúsculas; la contraseña es exacta.
     *
     * Al fallar enciende [_credencialesIncorrectas] para que la pantalla marque
     * los campos; al acertar apaga cualquier error previo y confirma el acceso.
     *
     * @return `true` si las credenciales coinciden y el ingreso puede continuar.
     */
    fun validarCredenciales(correoElectronico: String, contrasena: String): Boolean {
        val correoCoincide = correoElectronico.trim()
            .equals(CREDENCIAL_PRUEBA_CORREO, ignoreCase = true)
        val contrasenaCoincide = contrasena.trim() == CREDENCIAL_PRUEBA_CONTRASENA
        val ingresoValido = correoCoincide && contrasenaCoincide
        _credencialesIncorrectas.value = !ingresoValido
        return ingresoValido
    }

    /** Apaga el error de credenciales; se invoca al editar los campos del login. */
    fun limpiarErrorCredenciales() {
        _credencialesIncorrectas.value = false
    }

    /**
     * Sincroniza el registro principal de Room con el usuario autenticado.
     *
     * Se invoca tras un ingreso validado ([validarCredenciales] en `true`): la
     * fila principal se inserta (base vacía) o se sobrescribe (registro de una
     * persona distinta) con los datos básicos del usuario de prueba, de modo que
     * "Mi perfil" nunca muestre datos de un usuario anterior. Reutilizar el id de
     * la fila existente convierte la escritura en UPDATE sobre el mismo registro:
     * el observador reactivo de Room lo detecta y la vista se refresca sola.
     * Si el registro ya pertenece a este correo se conservan sus ediciones
     * (nombre, biografía y foto); solo se completa el nombre si quedó en blanco
     * de un sembrado previo.
     */
    fun registrarIngreso(correoElectronico: String) {
        val correoNormalizado = correoElectronico.trim().lowercase(Locale.ROOT)
        // Solo el ingreso autenticado reabre la sesión: los invitados usan
        // [ingresarComoInvitado] y no deben volver a exponer el perfil persistido.
        if (!ValidadorPerfil.esCorreoValido(correoNormalizado)) return
        _sesionActiva.value = true

        viewModelScope.launch {
            runCatching {
                val registroPrincipal = repositorioUsuario.observarUsuarioPrincipal().first()
                if (registroPrincipal != null &&
                    registroPrincipal.correoElectronico.trim()
                        .equals(correoNormalizado, ignoreCase = true)
                ) {
                    // El registro ya es de este usuario: se conservan sus datos.
                    if (registroPrincipal.nombreCompleto.isNotBlank()) return@runCatching

                    // Nombre en blanco de un sembrado antiguo: se completa sin
                    // tocar biografía ni foto.
                    repositorioUsuario.guardarUsuario(
                        registroPrincipal.copy(nombreCompleto = NOMBRE_USUARIO_PRUEBA)
                    )
                    return@runCatching
                }

                repositorioUsuario.guardarUsuario(
                    Usuario(
                        idUsuario = registroPrincipal?.idUsuario ?: Usuario.ID_SIN_ASIGNAR,
                        nombreCompleto = NOMBRE_USUARIO_PRUEBA,
                        correoElectronico = correoNormalizado,
                        biografia = ""
                    )
                )
                // Escritura confirmada en Room: recién ahora se libera la foto del
                // registro reemplazado para no dejar archivos huérfanos en filesDir.
                almacenFotosPerfil.eliminarFotoSiEsPropia(registroPrincipal?.uriFotoPerfil)
            }
            // Un fallo aquí no debe bloquear el ingreso: el perfil puede corregirse
            // desde "Editar perfil" (la pantalla de login no espera el resultado).
        }
    }

    /**
     * Prepara el modo invitado antes de navegar al Home.
     *
     * Apagar la sesión hace que [usuarioActual] emita `null` en memoria sin
     * borrar la fila de Room: "Mi perfil" muestra su estado vacío aunque exista
     * un perfil persistido de un ingreso anterior.
     */
    fun ingresarComoInvitado() {
        _sesionActiva.value = false
    }

    /**
     * Termina la sesión actual.
     *
     * El perfil persistido no se borra de Room: solo deja de exponerse a la UI
     * hasta el próximo [registrarIngreso], de modo que volver a ingresar con el
     * mismo correo recupera los datos tal como quedaron.
     */
    fun cerrarSesion() {
        _sesionActiva.value = false
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

        /** Credenciales fijas del usuario de prueba que exige la autenticación simulada. */
        const val CREDENCIAL_PRUEBA_CORREO = "contacto@cenamad.cl"
        const val CREDENCIAL_PRUEBA_CONTRASENA = "Admin123"

        /** Nombre con el que se siembra o sobrescribe el registro del usuario de prueba. */
        const val NOMBRE_USUARIO_PRUEBA = "Usuario CENAMAD"

        /**
         * Factory manual: obtiene el repositorio desde el contenedor de la Application
         * y lo inyecta en el constructor (sin librerías de DI).
         */
        val Fabrica: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BuscadorApplication
                UsuarioViewModel(
                    repositorioUsuario = aplicacion.contenedorAplicacion.repositorioUsuario,
                    almacenFotosPerfil = aplicacion.contenedorAplicacion.almacenFotosPerfil
                )
            }
        }
    }
}
