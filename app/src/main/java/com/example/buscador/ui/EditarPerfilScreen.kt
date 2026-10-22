package com.example.buscador.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.buscador.repository.AlmacenFotosPerfil
import com.example.buscador.model.Usuario
import com.example.buscador.model.ValidadorPerfil
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.GrisBordeCampo
import com.example.buscador.ui.theme.GrisTextoSecundario
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.YellowDuoc
import com.example.buscador.ui.components.BotonVolver
import com.example.buscador.ui.components.DialogoConfirmacion
import com.example.buscador.ui.components.FotoPerfil
import com.example.buscador.viewmodel.EstadoEdicionPerfil
import kotlinx.coroutines.launch

/**
 * Formulario de edición del perfil.
 *
 * Flujo de datos:
 * 1. [usuarioActual] llega desde el ViewModel (Room) como valor inicial.
 * 2. Los campos se editan con estado local de Compose (estado efímero de UI).
 * 3. Al guardar, se delega en [onGuardarCambios] -> ViewModel -> Repositorio -> Room.
 * 4. El ViewModel informa el resultado en [estadoEdicion]; solo cuando Room
 *    confirma la escritura se invoca [onGuardadoExitoso] y se vuelve al perfil.
 *
 * El formulario no decide si corresponde INSERT o UPDATE: solo entrega los datos
 * y el ViewModel resuelve la fila (nueva o existente) contra la base local.
 *
 * La foto se obtiene por dos vías, ambas resueltas con ActivityResultContracts:
 * - Cámara: se pide el permiso CAMERA en tiempo de ejecución (RequestPermission) y
 *   se abre la app de cámara con TakePicture sobre una URI de FileProvider.
 * - Galería: Photo Picker (PickVisualMedia), que no requiere permisos de almacenamiento.
 * En ambos casos la imagen queda en el almacenamiento privado de la app y a Room solo
 * viaja la URI resultante, por lo que la foto sigue disponible tras reiniciar.
 *
 * @param usuarioActual Usuario persistido o null si aún no existe.
 * @param estaCargando `true` mientras Room no entrega la primera lectura.
 * @param estadoEdicion Estado del guardado (inactivo, guardando, guardado o error).
 * @param onGuardarCambios Callback con los cuatro campos del perfil.
 * @param onGuardadoExitoso Se invoca cuando Room confirmó la escritura.
 * @param onLimpiarEstadoEdicion Devuelve el estado a "inactivo" al salir de la pantalla.
 * @param onVolver Regresa al perfil; si hay cambios sin guardar, la pantalla
 *   pide confirmación antes de descartarlos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    usuarioActual: Usuario?,
    estaCargando: Boolean,
    estadoEdicion: EstadoEdicionPerfil,
    onGuardarCambios: (
        nombreCompleto: String,
        correoElectronico: String,
        biografia: String,
        uriFotoPerfil: String?
    ) -> Unit,
    onGuardadoExitoso: () -> Unit,
    onLimpiarEstadoEdicion: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current
    val alcanceCorrutinas = rememberCoroutineScope()
    val almacenFotos = remember(contexto) { AlmacenFotosPerfil(contexto) }

    // rememberSaveable: el intent de cámara puede recrear la Activity (rotación o
    // muerte del proceso) y el formulario debe sobrevivir a ese regreso.
    var nombreCompleto by rememberSaveable { mutableStateOf("") }
    var correoElectronico by rememberSaveable { mutableStateOf("") }
    var biografia by rememberSaveable { mutableStateOf("") }
    var uriFotoPerfil by rememberSaveable { mutableStateOf<String?>(null) }
    var uriFotoOriginal by rememberSaveable { mutableStateOf<String?>(null) }
    var uriCamaraPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var formularioInicializado by rememberSaveable { mutableStateOf(false) }
    var guardadoSolicitado by rememberSaveable { mutableStateOf(false) }

    // Estados efímeros: no tiene sentido restaurarlos después de un reinicio.
    var procesandoFoto by remember { mutableStateOf(false) }
    var mensajeFoto by remember { mutableStateOf<String?>(null) }

    val estaGuardando = estadoEdicion is EstadoEdicionPerfil.Guardando
    val mensajeError = (estadoEdicion as? EstadoEdicionPerfil.Error)?.mensaje
    val dispositivoTieneCamara = remember(contexto) {
        contexto.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

    LaunchedEffect(Unit) {
        // Descarta el resultado de una sesión de edición anterior: si el usuario
        // salió mientras se guardaba, ese "Guardado" no debe navegar ahora.
        onLimpiarEstadoEdicion()
    }

    // Se navega con el resultado real de Room y solo si el guardado se pidió desde
    // esta pantalla; así se evitan navegaciones fantasma al reingresar.
    LaunchedEffect(estadoEdicion) {
        if (guardadoSolicitado && estadoEdicion is EstadoEdicionPerfil.Guardado) {
            // Room ya confirmó la escritura: la foto anterior quedó huérfana y se
            // libera aquí, nunca antes (si el guardado falla, la fila la sigue usando).
            if (uriFotoOriginal != null && uriFotoOriginal != uriFotoPerfil) {
                almacenFotos.eliminarFotoSiEsPropia(uriFotoOriginal)
            }
            onGuardadoExitoso()
        }
    }

    // Al desmontar la pantalla (Volver, gesto atrás o guardado confirmado) el estado
    // se reinicia para que "Guardado" no vuelva a navegar en un ingreso posterior.
    DisposableEffect(Unit) {
        onDispose { onLimpiarEstadoEdicion() }
    }

    // Carga inicial desde Room. La bandera evita sobrescribir lo que el usuario
    // ya escribió si el Flow vuelve a emitir mientras edita.
    LaunchedEffect(usuarioActual) {
        val usuario = usuarioActual
        if (!formularioInicializado && usuario != null) {
            nombreCompleto = usuario.nombreCompleto
            correoElectronico = usuario.correoElectronico
            biografia = usuario.biografia
            uriFotoPerfil = usuario.uriFotoPerfil
            uriFotoOriginal = usuario.uriFotoPerfil
            formularioInicializado = true
        }
    }

    // Aplica una foto recién elegida y libera la copia intermedia si ya nadie la usa.
    // La foto original (la que está en Room) no se toca hasta que el guardado confirme.
    fun aplicarFotoSeleccionada(uriNueva: String) {
        val uriAnterior = uriFotoPerfil
        uriFotoPerfil = uriNueva
        if (uriAnterior != null && uriAnterior != uriNueva && uriAnterior != uriFotoOriginal) {
            almacenFotos.eliminarFotoSiEsPropia(uriAnterior)
        }
    }

    // TakePicture devuelve true solo si la app de cámara escribió sobre la URI que le
    // entregamos; si el usuario cancela, se borra el archivo vacío de destino.
    val lanzadorCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { capturaExitosa ->
        val uriCaptura = uriCamaraPendiente
        uriCamaraPendiente = null
        if (capturaExitosa && uriCaptura != null) {
            aplicarFotoSeleccionada(uriCaptura)
        } else {
            almacenFotos.eliminarFotoSiEsPropia(uriCaptura)
        }
    }

    // Crea el archivo destino en el almacenamiento propio y abre la cámara del sistema.
    fun abrirCamara() {
        val uriDestino = try {
            almacenFotos.crearUriParaCamara()
        } catch (error: Exception) {
            // FileProvider sin ruta configurada: no hay dónde escribir la captura.
            Log.w(ETIQUETA_LOG, "No se pudo crear la URI de la captura.", error)
            mensajeFoto = MENSAJE_CAMARA_NO_DISPONIBLE
            return
        }

        uriCamaraPendiente = uriDestino.toString()
        mensajeFoto = null
        try {
            lanzadorCamara.launch(uriDestino)
        } catch (error: Exception) {
            // Típicamente ActivityNotFoundException: no hay app de cámara instalada.
            uriCamaraPendiente = null
            almacenFotos.eliminarFotoSiEsPropia(uriDestino.toString())
            Log.w(ETIQUETA_LOG, "No se pudo abrir la app de cámara.", error)
            mensajeFoto = MENSAJE_CAMARA_NO_DISPONIBLE
        }
    }

    // La galería no pide permisos: Photo Picker entrega una URI temporal. Se copia al
    // almacenamiento propio para que Room guarde una referencia que no caduque.
    val selectorGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uriSeleccionada ->
        if (uriSeleccionada == null) return@rememberLauncherForActivityResult

        alcanceCorrutinas.launch {
            procesandoFoto = true
            val uriCopiada = almacenFotos.copiarDesdeGaleria(uriSeleccionada)
            procesandoFoto = false

            if (uriCopiada != null) {
                aplicarFotoSeleccionada(uriCopiada.toString())
                mensajeFoto = null
            } else {
                mensajeFoto = MENSAJE_GALERIA
            }
        }
    }

    // Concedido el permiso se abre la cámara; si lo rechaza (o lo bloqueó antes),
    // se explica al usuario que puede habilitarlo desde los ajustes del sistema.
    val lanzadorPermisoCamara = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { permisoConcedido ->
        if (permisoConcedido) {
            abrirCamara()
        } else {
            mensajeFoto = MENSAJE_PERMISO_CAMARA
        }
    }

    // Mismo criterio que aplica el ViewModel antes de escribir en Room.
    val correoInvalido = correoElectronico.isNotBlank() &&
        !ValidadorPerfil.esCorreoValido(correoElectronico)
    val formularioValido = ValidadorPerfil.esNombreValido(nombreCompleto) &&
        ValidadorPerfil.esCorreoValido(correoElectronico)

    // `true` cuando el formulario difiere del perfil leído en Room (o ya acumuló
    // datos de un perfil nuevo que todavía no existe en la base local). Se
    // recalcula en cada recomposición, así que cubre tanto texto como foto.
    val hayCambiosSinGuardar = when (val usuario = usuarioActual) {
        null -> nombreCompleto.isNotBlank() || correoElectronico.isNotBlank() ||
            biografia.isNotBlank() || uriFotoPerfil != null
        else -> nombreCompleto != usuario.nombreCompleto ||
            correoElectronico != usuario.correoElectronico ||
            biografia != usuario.biografia ||
            uriFotoPerfil != usuario.uriFotoPerfil
    }

    // Visibilidad del diálogo de descarte: sobrevive a rotaciones y se oculta
    // tanto al confirmar como al cancelar.
    var dialogoDescarteVisible by rememberSaveable { mutableStateOf(false) }

    // Salir del formulario: con cambios sin guardar se pide confirmación para no
    // perder trabajo por accidente; sin cambios se regresa de inmediato. Mientras
    // se guarda no se pregunta: la escritura ya va en camino hacia Room.
    fun intentarVolver() {
        if (hayCambiosSinGuardar && !estaGuardando) {
            dialogoDescarteVisible = true
        } else {
            onVolver()
        }
    }

    // El gesto atrás del sistema sigue el mismo criterio que el botón "Volver":
    // habilitado solo cuando hay algo que confirmar; en caso contrario el
    // comportamiento por defecto (popBackStack del NavHost) queda intacto.
    BackHandler(enabled = hayCambiosSinGuardar && !estaGuardando) {
        dialogoDescarteVisible = true
    }

    // Descartar es una acción con consecuencias: se confirma antes de ejecutarla.
    // El toque fuera del diálogo cuenta como "Cancelar" y conserva los cambios.
    if (dialogoDescarteVisible) {
        DialogoConfirmacion(
            titulo = "Descartar cambios",
            mensaje = "Tienes cambios sin guardar. Si sales ahora se perderá lo " +
                "que escribiste en el formulario y el perfil quedará como estaba.",
            onConfirmar = {
                dialogoDescarteVisible = false
                // La foto elegida vive solo en el almacenamiento propio de la app:
                // al descartar ya nadie la referencia y se libera. La original
                // (la de Room) no se toca, igual que al guardar con éxito.
                if (uriFotoPerfil != null && uriFotoPerfil != uriFotoOriginal) {
                    almacenFotos.eliminarFotoSiEsPropia(uriFotoPerfil)
                }
                onVolver()
            },
            onCancelar = { dialogoDescarteVisible = false }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            TopAppBar(
                title = { Text(text = "Editar perfil") },
                navigationIcon = { BotonVolver(onVolver = { intentarVolver() }) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingInterno ->
        if (estaCargando) {
            // El formulario no se muestra hasta conocer el perfil real: así una
            // emisión tardía de Room no sobrescribe lo que el usuario escribe.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkGreen)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(all = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            FotoPerfil(
                uriFotoPerfil = uriFotoPerfil,
                nombreCompleto = nombreCompleto
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        mensajeFoto = null
                        when {
                            !dispositivoTieneCamara ->
                                mensajeFoto = MENSAJE_CAMARA_NO_DISPONIBLE
                            ContextCompat.checkSelfPermission(
                                contexto,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED -> abrirCamara()
                            else -> lanzadorPermisoCamara.launch(Manifest.permission.CAMERA)
                        }
                    },
                    enabled = !procesandoFoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCamera,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Tomar foto")
                }

                OutlinedButton(
                    onClick = {
                        selectorGaleria.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    enabled = !procesandoFoto,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoLibrary,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Galería")
                }
            }

            if (procesandoFoto) {
                Text(
                    text = "Procesando imagen…",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (mensajeFoto != null) {
                // Aviso de permiso rechazado, cámara ausente o copia fallida.
                Text(
                    text = mensajeFoto.orEmpty(),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombreCompleto,
                onValueChange = { nuevoNombre -> nombreCompleto = nuevoNombre },
                label = { Text(text = "Nombre completo") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Person,
                        contentDescription = null,
                        modifier = Modifier.size(size = 20.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = coloresCampoPerfil()
            )

            OutlinedTextField(
                value = correoElectronico,
                onValueChange = { nuevoCorreo -> correoElectronico = nuevoCorreo },
                label = { Text(text = "Correo electrónico") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(size = 20.dp)
                    )
                },
                singleLine = true,
                isError = correoInvalido,
                supportingText = { Text(text = "Ejemplo: nombre@cenamad.cl") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(),
                colors = coloresCampoPerfil()
            )

            OutlinedTextField(
                value = biografia,
                onValueChange = { nuevaBiografia -> biografia = nuevaBiografia },
                label = { Text(text = "Biografía") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(size = 20.dp)
                    )
                },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                colors = coloresCampoPerfil()
            )

            if (mensajeError != null) {
                // Error de validación o de Room (por ejemplo, correo ya registrado).
                Text(
                    text = mensajeError,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    guardadoSolicitado = true
                    onGuardarCambios(
                        nombreCompleto,
                        correoElectronico,
                        biografia,
                        uriFotoPerfil
                    )
                },
                enabled = formularioValido && !estaGuardando && !procesandoFoto,
                colors = ButtonDefaults.buttonColors(
                    containerColor = YellowDuoc,
                    contentColor = Color.Black,
                    disabledContainerColor = YellowDuoc.copy(alpha = 0.45f),
                    disabledContentColor = Color.Black.copy(alpha = 0.55f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = when {
                        estaGuardando -> "Guardando…"
                        usuarioActual == null -> "Crear perfil"
                        else -> "Guardar cambios"
                    },
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Paleta compartida por los campos del formulario.
 *
 * En reposo el borde y los iconos son neutros; al recibir el foco, el borde,
 * la etiqueta, el icono guía y el cursor pasan al color primario (verde
 * institucional) para reforzar el contraste del campo activo.
 */
@Composable
private fun coloresCampoPerfil() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    focusedLabelColor = MaterialTheme.colorScheme.primary,
    focusedLeadingIconColor = MaterialTheme.colorScheme.primary,
    focusedSupportingTextColor = MaterialTheme.colorScheme.primary,
    cursorColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = GrisBordeCampo,
    unfocusedLabelColor = GrisTextoSecundario,
    unfocusedLeadingIconColor = GrisTextoSecundario
)

/** Etiqueta de Log para diagnosticar fallos al crear la URI o abrir la cámara. */
private const val ETIQUETA_LOG = "EditarPerfilScreen"

/** No hay cámara en el equipo o su intent no pudo resolverse. */
private const val MENSAJE_CAMARA_NO_DISPONIBLE =
    "No se pudo abrir la cámara. Verifica que el dispositivo tenga una app de cámara."

/** El usuario rechazó el permiso CAMERA o lo bloqueó en los ajustes. */
private const val MENSAJE_PERMISO_CAMARA =
    "Para tomar la foto debes permitir el acceso a la cámara desde los ajustes de la app."

/** La copia de la imagen elegida falló (archivo ilegible o sin espacio). */
private const val MENSAJE_GALERIA =
    "No se pudo cargar la imagen seleccionada. Intenta con otra."
