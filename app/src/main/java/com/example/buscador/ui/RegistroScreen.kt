package com.example.buscador.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.ValidadorPerfil
import com.example.buscador.ui.components.BotonVolver
import com.example.buscador.ui.components.LogoSolCenamad
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.DoradoCenamad
import com.example.buscador.ui.theme.GrisBordeCampo
import com.example.buscador.ui.theme.GrisTextoSecundario
import com.example.buscador.ui.theme.NegroTexto
import com.example.buscador.ui.theme.White
import com.example.buscador.viewmodel.EstadoRegistro

/**
 * Formulario de registro de una cuenta nueva.
 *
 * Flujo de datos (MVVM unidireccional):
 * 1. La pantalla no declara estado de formulario propio: cada campo y cada mensaje
 *    de error se leen de [estado], el [EstadoRegistro] que expone el ViewModel.
 * 2. Las validaciones son reactivas porque el ViewModel las recalcula en cada
 *    pulsación ([onNombreCambio] / [onCorreoCambio] / [onContrasenaCambio]);
 *    el TextField muestra su borde rojo y su `supportingText` de inmediato.
 * 3. Al presionar CREAR CUENTA, [onRegistrar] inserta en Room; cuando la escritura
 *    se confirma, el correo registrado aparece en el estado y la pantalla invoca
 *    [onRegistroExitoso] para que el NavHost cierre el flujo y navegue.
 * 4. Al desmontar, [onLimpiarEstado] devuelve el formulario a su estado base para
 *    que un registro anterior no vuelva a navegar al reingresar.
 *
 * @param estado Estado completo del formulario vindo del ViewModel.
 * @param onNombreCambio Nuevo texto del campo Nombre.
 * @param onCorreoCambio Nuevo texto del campo Correo.
 * @param onContrasenaCambio Nuevo texto del campo Contraseña.
 * @param onRegistrar Solicita el alta de la cuenta (INSERT en Room).
 * @param onRegistroExitoso Se invoca con el correo cuando Room confirmó el alta.
 * @param onLimpiarEstado Reinicia el estado del registro al salir de la pantalla.
 * @param onVolver Regresa al login (popBackStack desde el NavHost).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    estado: EstadoRegistro,
    onNombreCambio: (String) -> Unit,
    onCorreoCambio: (String) -> Unit,
    onContrasenaCambio: (String) -> Unit,
    onRegistrar: () -> Unit,
    onRegistroExitoso: (correoElectronico: String) -> Unit,
    onLimpiarEstado: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Estado efímero de UI: la visibilidad de la contraseña no es dato de negocio,
    // así que vive aquí y no en el ViewModel.
    var contrasenaEsVisible by remember { mutableStateOf(false) }

    // Un solo disparador de navegación: la corrutina se reevalúa si el correo
    // registrado cambia, pero el estado se limpia al desmontar la pantalla.
    LaunchedEffect(estado.correoRegistrado) {
        estado.correoRegistrado?.let { correo -> onRegistroExitoso(correo) }
    }

    DisposableEffect(Unit) {
        onDispose { onLimpiarEstado() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Crear cuenta",
                        color = White,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = { BotonVolver(onVolver = onVolver) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkGreen)
            )
        },
        modifier = modifier
    ) { valoresBorde ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(White)
                .padding(valoresBorde)
                .imePadding()
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 32.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Identidad del formulario: mismo lenguaje visual que el login.
            LogoSolCenamad(tamano = 56.dp)
            Spacer(modifier = Modifier.height(height = 12.dp))
            Text(
                text = "Regístrate en CENAMAD",
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkGreen
            )
            Spacer(modifier = Modifier.height(height = 4.dp))
            Text(
                text = "Crea tus credenciales para guardar tu perfil",
                fontSize = 13.sp,
                color = GrisTextoSecundario
            )

            Spacer(modifier = Modifier.height(height = 32.dp))

            OutlinedTextField(
                value = estado.nombre,
                onValueChange = onNombreCambio,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Nombre") },
                placeholder = { Text(text = "Nombre y apellidos") },
                isError = estado.errorNombre != null,
                supportingText = { MensajeCampoRegistro(estado.errorNombre) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(percent = 50),
                colors = coloresCampoRegistro()
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            OutlinedTextField(
                value = estado.correo,
                onValueChange = onCorreoCambio,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Correo") },
                placeholder = { Text(text = "nombre@cenamad.cl") },
                isError = estado.errorCorreo != null,
                supportingText = { MensajeCampoRegistro(estado.errorCorreo) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                shape = RoundedCornerShape(percent = 50),
                colors = coloresCampoRegistro()
            )

            Spacer(modifier = Modifier.height(height = 16.dp))

            OutlinedTextField(
                value = estado.contrasena,
                onValueChange = onContrasenaCambio,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(text = "Contraseña") },
                placeholder = {
                    Text(
                        text = "Mínimo ${ValidadorPerfil.LONGITUD_MINIMA_CONTRASENA} caracteres"
                    )
                },
                isError = estado.errorContrasena != null,
                supportingText = { MensajeCampoRegistro(estado.errorContrasena) },
                singleLine = true,
                visualTransformation = if (contrasenaEsVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (estado.formularioEsValido && !estado.estaRegistrando) {
                            onRegistrar()
                        }
                    }
                ),
                trailingIcon = {
                    IconButton(onClick = { contrasenaEsVisible = !contrasenaEsVisible }) {
                        Icon(
                            imageVector = if (contrasenaEsVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = if (contrasenaEsVisible) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            },
                            tint = GrisTextoSecundario
                        )
                    }
                },
                shape = RoundedCornerShape(percent = 50),
                colors = coloresCampoRegistro()
            )

            // Error general de la escritura (correo duplicado, fallo de Room):
            // pertenece al intento completo, no a un campo, así que se pinta suelto.
            estado.mensajeError?.let { mensaje ->
                Spacer(modifier = Modifier.height(height = 16.dp))
                Text(
                    text = mensaje,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(height = 32.dp))

            Button(
                onClick = onRegistrar,
                enabled = estado.formularioEsValido && !estado.estaRegistrando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DoradoCenamad,
                    contentColor = White,
                    disabledContainerColor = GrisBordeCampo.copy(alpha = 0.5f),
                    disabledContentColor = White
                ),
                shape = RoundedCornerShape(percent = 50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 52.dp)
            ) {
                if (estado.estaRegistrando) {
                    CircularProgressIndicator(
                        modifier = Modifier.width(width = 22.dp),
                        color = White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "CREAR CUENTA",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

/**
 * Paleta de colores de los campos del registro, compartida con el mismo criterio
 * del login: borde neutro que vira a verde institucional al enfocar.
 */
@Composable
private fun coloresCampoRegistro() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DarkGreen,
    unfocusedBorderColor = GrisBordeCampo,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    focusedPlaceholderColor = GrisTextoSecundario,
    unfocusedPlaceholderColor = GrisTextoSecundario,
    focusedLabelColor = DarkGreen,
    unfocusedLabelColor = GrisTextoSecundario,
    focusedTextColor = NegroTexto,
    unfocusedTextColor = NegroTexto,
    cursorColor = DarkGreen
)

/**
 * Mensaje reactivo de un campo del registro.
 *
 * Solo emite contenido cuando el ViewModel calculó un error para ese campo;
 * Material 3 lo pinta con el color de error porque el campo está marcado con
 * `isError`, cumpliendo la retroalimentación visual inmediata exigida.
 */
@Composable
private fun MensajeCampoRegistro(mensaje: String?) {
    if (mensaje != null) {
        Text(text = mensaje)
    }
}
