package com.example.buscador.view.perfil

import android.content.Intent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.Usuario
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.YellowDuoc
import com.example.buscador.view.components.BotonVolver
import com.example.buscador.view.components.FotoPerfil

/**
 * Formulario de edición del perfil.
 *
 * Flujo de datos:
 * 1. [usuarioActual] llega desde el ViewModel (Room) como valor inicial.
 * 2. Los campos se editan con estado local de Compose.
 * 3. Al guardar, se delega en [onGuardarCambios] -> ViewModel -> Repositorio -> Room.
 * 4. Room emite el cambio y la app vuelve a MiPerfil ya actualizado.
 *
 * La foto se elige con el selector del sistema (Photo Picker), por lo que
 * no se requieren permisos de almacenamiento en el manifiesto.
 *
 * @param usuarioActual Usuario persistido o null si aún no existe.
 * @param onGuardarCambios Callback con los cuatro campos del perfil.
 * @param onVolver Regresa al perfil sin guardar.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarPerfilScreen(
    usuarioActual: Usuario?,
    onGuardarCambios: (
        nombreCompleto: String,
        correoElectronico: String,
        biografia: String,
        uriFotoPerfil: String?
    ) -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    var nombreCompleto by remember { mutableStateOf("") }
    var correoElectronico by remember { mutableStateOf("") }
    var biografia by remember { mutableStateOf("") }
    var uriFotoPerfil by remember { mutableStateOf<String?>(null) }
    var formularioInicializado by remember { mutableStateOf(false) }

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

    val correoInvalido = correoElectronico.isNotBlank() && !correoElectronico.contains("@")
    val formularioValido = nombreCompleto.isNotBlank() && correoElectronico.contains("@")

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

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    onGuardarCambios(
                        nombreCompleto,
                        correoElectronico,
                        biografia,
                        uriFotoPerfil
                    )
                },
                enabled = formularioValido,
                colors = ButtonDefaults.buttonColors(containerColor = YellowDuoc),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Guardar cambios",
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/** Etiqueta de Log para diagnosticar el permiso de la URI de la foto. */
private const val ETIQUETA_LOG = "EditarPerfilScreen"
