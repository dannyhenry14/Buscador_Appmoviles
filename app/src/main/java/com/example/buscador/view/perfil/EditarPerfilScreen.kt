package com.example.buscador.view.perfil

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.Usuario
import com.example.buscador.model.ValidadorPerfil
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.YellowDuoc
import com.example.buscador.view.components.BotonVolver
import com.example.buscador.view.components.FotoPerfil
import com.example.buscador.viewmodel.EstadoEdicionPerfil

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
 * La foto se elige con el selector del sistema (Photo Picker), por lo que
 * no se requieren permisos de almacenamiento en el manifiesto.
 *
 * @param usuarioActual Usuario persistido o null si aún no existe.
 * @param estaCargando `true` mientras Room no entrega la primera lectura.
 * @param estadoEdicion Estado del guardado (inactivo, guardando, guardado o error).
 * @param onGuardarCambios Callback con los cuatro campos del perfil.
 * @param onGuardadoExitoso Se invoca cuando Room confirmó la escritura.
 * @param onLimpiarEstadoEdicion Devuelve el estado a "inactivo" al salir de la pantalla.
 * @param onVolver Regresa al perfil sin guardar.
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

    var nombreCompleto by remember { mutableStateOf("") }
    var correoElectronico by remember { mutableStateOf("") }
    var biografia by remember { mutableStateOf("") }
    var uriFotoPerfil by remember { mutableStateOf<String?>(null) }
    var formularioInicializado by remember { mutableStateOf(false) }
    var guardadoSolicitado by remember { mutableStateOf(false) }

    val estaGuardando = estadoEdicion is EstadoEdicionPerfil.Guardando
    val mensajeError = (estadoEdicion as? EstadoEdicionPerfil.Error)?.mensaje

    LaunchedEffect(Unit) {
        // Descarta el resultado de una sesión de edición anterior: si el usuario
        // salió mientras se guardaba, ese "Guardado" no debe navegar ahora.
        onLimpiarEstadoEdicion()
    }

    // Se navega con el resultado real de Room y solo si el guardado se pidió desde
    // esta pantalla; así se evitan navegaciones fantasma al reingresar.
    LaunchedEffect(estadoEdicion) {
        if (guardadoSolicitado && estadoEdicion is EstadoEdicionPerfil.Guardado) {
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
            formularioInicializado = true
        }
    }

    val selectorFotoPerfil = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uriSeleccionada ->
        if (uriSeleccionada != null) {
            // El selector entrega un permiso temporal: se intenta persistir para que
            // la URI siga siendo legible después de reiniciar el proceso.
            try {
                contexto.contentResolver.takePersistableUriPermission(
                    uriSeleccionada,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (excepcion: SecurityException) {
                Log.w(ETIQUETA_LOG, "No se pudo persistir el permiso de la foto.", excepcion)
            }
            uriFotoPerfil = uriSeleccionada.toString()
        }
    }

    // Mismo criterio que aplica el ViewModel antes de escribir en Room.
    val correoInvalido = correoElectronico.isNotBlank() &&
        !ValidadorPerfil.esCorreoValido(correoElectronico)
    val formularioValido = ValidadorPerfil.esNombreValido(nombreCompleto) &&
        ValidadorPerfil.esCorreoValido(correoElectronico)

    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            TopAppBar(
                title = { Text(text = "Editar perfil") },
                navigationIcon = { BotonVolver(onVolver = onVolver) },
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

            OutlinedButton(
                onClick = {
                    selectorFotoPerfil.launch(
                        PickVisualMediaRequest(
                            mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Seleccionar foto de perfil")
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = nombreCompleto,
                onValueChange = { nuevoNombre -> nombreCompleto = nuevoNombre },
                label = { Text(text = "Nombre completo") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = correoElectronico,
                onValueChange = { nuevoCorreo -> correoElectronico = nuevoCorreo },
                label = { Text(text = "Correo electrónico") },
                singleLine = true,
                isError = correoInvalido,
                supportingText = { Text(text = "Ejemplo: nombre@cenamad.cl") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = biografia,
                onValueChange = { nuevaBiografia -> biografia = nuevaBiografia },
                label = { Text(text = "Biografía") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
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
                enabled = formularioValido && !estaGuardando,
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

/** Etiqueta de Log para diagnosticar el permiso de la URI de la foto. */
private const val ETIQUETA_LOG = "EditarPerfilScreen"
