package com.example.buscador.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.ui.theme.AzulDuoc
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.DoradoCenamad
import com.example.buscador.ui.theme.GrisBordeCampo
import com.example.buscador.ui.theme.GrisTextoSecundario
import com.example.buscador.ui.theme.NegroTexto
import com.example.buscador.ui.theme.White
import com.example.buscador.ui.theme.YellowDuoc
import com.example.buscador.view.components.LogoSolCenamad
import com.example.buscador.view.components.MarcaInstitucional

/**
 * Pantalla de inicio de sesión con diseño minimalista.
 *
 * Estructura:
 * 1. Encabezado: franja verde institucional limpia con el wordmark CENAMAD.
 * 2. Contenido centrado: logo sol, título, formulario y accesos secundarios.
 * 3. Pie de pantalla: marca "Duoc UC | CITT" una única vez, centrada.
 *
 * No hay dibujos decorativos de Canvas salvo el logo sol; la pantalla usa
 * fondo blanco y jerarquía tipográfica.
 *
 * Stateless respecto a la navegación: expone [onLoginSuccess],
 * [onIngresarComoInvitado] y [onNavigateToRegister] y delega las rutas al
 * NavHost (la vista no conoce rutas). El estado del formulario es local porque
 * es efímero (no sobrevive a la pantalla).
 *
 * Validación: el botón INGRESAR solo se habilita cuando ambos campos tienen
 * contenido ([String.isNotBlank], ignora espacios en blanco). Las credenciales
 * se comprueban en el ViewModel: si el último intento fue rechazado
 * ([credencialesIncorrectas]), ambos campos se marcan en rojo con el texto
 * "Correo o contraseña incorrectos" hasta que el usuario vuelva a escribir.
 *
 * @param credencialesIncorrectas `true` cuando el ViewModel rechazó el último
 *   intento de ingreso; activa el error visual de ambos campos.
 * @param onLoginSuccess Invocado con el correo y la contraseña capturados al
 *   presionar INGRESAR (o al confirmar con el teclado). El NavHost valida las
 *   credenciales y solo navega si son correctas.
 * @param onIngresarComoInvitado Invocado desde el enlace "Ingresar como
 *   invitado"; el NavHost anula el usuario activo antes de navegar.
 * @param onLimpiarErrorCredenciales Apaga el error visual; se invoca al editar
 *   cualquiera de los campos.
 * @param onNavigateToRegister Invocado desde el enlace "más información".
 * @param avisoRegistro Aviso contextual opcional que reemplaza al mensaje de
 *   registro por defecto. La pantalla muestra siempre el banner informativo:
 *   si llega nulo o vacío se usa [AVISO_REGISTRO_POR_DEFECTO], así que el
 *   mensaje aparece al abrir la app sin depender de un argumento de navegación.
 */
@Composable
fun LoginScreen(
    credencialesIncorrectas: Boolean,
    onLoginSuccess: (correoElectronico: String, contrasena: String) -> Unit,
    onIngresarComoInvitado: () -> Unit,
    onLimpiarErrorCredenciales: () -> Unit,
    onNavigateToRegister: () -> Unit,
    avisoRegistro: String? = null,
    modifier: Modifier = Modifier
) {
    var correoElectronico by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var contrasenaEsVisible by remember { mutableStateOf(false) }

    // El banner está visible desde el primer arranque del login; un aviso
    // recibido por navegación (por ejemplo, desde el perfil invitado) tiene
    // prioridad y sustituye al texto por defecto.
    val textoAviso = avisoRegistro?.takeIf { aviso -> aviso.isNotBlank() }
        ?: AVISO_REGISTRO_POR_DEFECTO

    // Regla de validación: ambos campos deben tener contenido real (no solo espacios).
    val formularioEsValido =
        correoElectronico.isNotBlank() && contrasena.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(White)
    ) {
        EncabezadoLogin()

        // Contenido central: centrado verticalmente, scrolleable y con imePadding
        // para que los campos sigan visibles cuando aparece el teclado.
        Box(
            modifier = Modifier
                .weight(weight = 1f)
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .align(alignment = Alignment.Center)
                    .verticalScroll(state = rememberScrollState())
                    .padding(horizontal = 32.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Bloque de identidad: logo sol + título + subtítulo institucional.
                LogoSolCenamad(tamano = 72.dp)
                Spacer(modifier = Modifier.height(height = 16.dp))
                Text(
                    text = "CENAMAD",
                    color = DarkGreen,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(height = 4.dp))
                Text(
                    text = "Universidad de San Joaquín",
                    color = GrisTextoSecundario,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(height = 40.dp))

                // Banner informativo: visible siempre, con el texto por defecto
                // o con el aviso que llegó por navegación.
                Text(
                    text = textoAviso,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = DarkGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = DoradoCenamad.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(size = 12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
                Spacer(modifier = Modifier.height(height = 24.dp))

                OutlinedTextField(
                    value = correoElectronico,
                    onValueChange = { nuevoValor ->
                        correoElectronico = nuevoValor
                        onLimpiarErrorCredenciales()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Correo o Usuario") },
                    isError = credencialesIncorrectas,
                    supportingText = { MensajeErrorCredenciales(credencialesIncorrectas) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(percent = 50),
                    colors = coloresCampoLogin()
                )

                Spacer(modifier = Modifier.height(height = 16.dp))

                OutlinedTextField(
                    value = contrasena,
                    onValueChange = { nuevoValor ->
                        contrasena = nuevoValor
                        onLimpiarErrorCredenciales()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(text = "Contraseña") },
                    isError = credencialesIncorrectas,
                    supportingText = { MensajeErrorCredenciales(credencialesIncorrectas) },
                    singleLine = true,
                    // El campo oculta los caracteres por defecto; el ícono del ojo
                    // alterna a VisualTransformation.None para mostrarlos.
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
                            if (formularioEsValido) {
                                onLoginSuccess(correoElectronico.trim(), contrasena)
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
                    colors = coloresCampoLogin()
                )

                // Enlace de recuperación alineado a la derecha, debajo del campo.
                Box(modifier = Modifier.fillMaxWidth()) {
                    TextButton(
                        // TODO: definir el flujo de recuperación de contraseña cuando exista.
                        onClick = { },
                        modifier = Modifier.align(alignment = Alignment.CenterEnd)
                    ) {
                        Text(
                            text = "¿Olvidaste tu contraseña?",
                            color = GrisTextoSecundario,
                            fontSize = 13.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(height = 24.dp))

                Button(
                    onClick = { onLoginSuccess(correoElectronico.trim(), contrasena) },
                    enabled = formularioEsValido,
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
                    Text(
                        text = "INGRESAR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                }

                Spacer(modifier = Modifier.height(height = 18.dp))

                TextoAccesosSecundarios(
                    onIngresarComoInvitado = onIngresarComoInvitado,
                    onAbrirMasInformacion = onNavigateToRegister
                )
            }
        }

        // Marca institucional: única mención en toda la pantalla, en el pie.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            MarcaInstitucional(
                tamanoTexto = 16.sp,
                colorDuoc = YellowDuoc,
                colorUC = AzulDuoc,
                colorSeparador = GrisBordeCampo,
                colorCITT = DarkGreen
            )
        }
    }
}

/**
 * Encabezado minimalista: franja verde institucional con el wordmark CENAMAD
 * en blanco (logo sol + nombre). Sin dibujos decorativos.
 */
@Composable
private fun EncabezadoLogin(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height = 110.dp)
            .background(DarkGreen)
    ) {
        Row(
            modifier = Modifier
                .align(alignment = Alignment.CenterStart)
                .statusBarsPadding()
                .padding(start = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LogoSolCenamad(tamano = 20.dp, colorPrimario = White, colorSecundario = White)
            Spacer(modifier = Modifier.width(width = 8.dp))
            Text(
                text = "CENAMAD",
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Paleta de colores compartida por los dos campos del formulario.
 *
 * Extraída a una función para que correo y contraseña mantengan el mismo
 * aspecto (borde redondeado neutro que vira a verde institucional al enfocar).
 */
@Composable
private fun coloresCampoLogin() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = DarkGreen,
    unfocusedBorderColor = GrisBordeCampo,
    focusedContainerColor = White,
    unfocusedContainerColor = White,
    focusedPlaceholderColor = GrisTextoSecundario,
    unfocusedPlaceholderColor = GrisTextoSecundario,
    focusedTextColor = NegroTexto,
    unfocusedTextColor = NegroTexto,
    cursorColor = DarkGreen
)


/**
 * Texto de apoyo del error de credenciales.
 *
 * Solo emite contenido cuando el último intento fue rechazado; Material 3 lo
 * pinta en el color de error automáticamente porque los campos están marcados
 * con `isError`, cumpliendo la retroalimentación visual exigida.
 */
@Composable
private fun MensajeErrorCredenciales(credencialesIncorrectas: Boolean) {
    if (credencialesIncorrectas) {
        Text(text = "Correo o contraseña incorrectos")
    }
}

/**
 * Línea de accesos secundarios del login: "Ingresar como invitado" y
 * "más información" como enlaces sobre un mismo texto.
 *
 * Implementada con `buildAnnotatedString` + `withLink` (API actual de
 * Foundation); cada span clickable invoca su callback sin subrayado.
 */
@Composable
private fun TextoAccesosSecundarios(
    onIngresarComoInvitado: () -> Unit,
    onAbrirMasInformacion: () -> Unit
) {
    val textoAccesos = buildAnnotatedString {
        withLink(
            LinkAnnotation.Clickable(
                tag = "invitado",
                linkInteractionListener = { onIngresarComoInvitado() }
            )
        ) {
            withStyle(style = SpanStyle(color = DarkGreen, fontWeight = FontWeight.SemiBold)) {
                append("Ingresar como invitado")
            }
        }
        withStyle(style = SpanStyle(color = GrisTextoSecundario)) {
            append(" o ")
        }
        withLink(
            LinkAnnotation.Clickable(
                tag = "mas_informacion",
                linkInteractionListener = { onAbrirMasInformacion() }
            )
        ) {
            withStyle(style = SpanStyle(color = GrisTextoSecundario)) {
                append("más información")
            }
        }
    }
    Text(text = textoAccesos, fontSize = 14.sp)
}

/**
 * Texto por defecto del banner informativo del login.
 *
 * Se muestra al abrir la aplicación por primera vez, sin necesidad de un
 * argumento de navegación, para explicar cómo registrar un usuario.
 */
private const val AVISO_REGISTRO_POR_DEFECTO =
    "Para registrar tu usuario, inicia sesión con tus credenciales y completa tu perfil."