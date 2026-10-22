package com.example.buscador.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.Usuario
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.YellowDuoc
import com.example.buscador.ui.components.BarraSuperiorSeccion
import com.example.buscador.ui.components.DialogoConfirmacion
import com.example.buscador.ui.components.FotoPerfil

/**
 * Perfil del usuario.
 *
 * Presenta dos estados excluyentes: para cuentas registradas muestra los
 * datos persistidos en Room (foto, nombre, biografía) con "Editar perfil" y
 * "Cerrar sesión"; para invitados muestra solo el aviso "Estás navegando
 * como invitado" y el acceso a "Registrar usuario", sin biografía ni sesión
 * que cerrar.
 *
 * Es una pantalla de solo lectura: el usuario llega desde
 * [com.example.buscador.viewmodel.UsuarioViewModel] a través del NavHost.
 * No mantiene copia local de los datos, por lo que Room es la única fuente
 * de verdad y la vista se refresca sola cada vez que cambia una fila.
 *
 * Es una sección de nivel superior: se llega desde el menú lateral, por lo
 * que la barra expone el botón hamburguesa en lugar de "Volver".
 *
 * @param usuario Usuario observado en Room; `null` cuando la sesión pertenece
 *   a un invitado (o Room aún no tiene fila), lo que activa la vista invitado.
 * @param estaCargando `true` mientras Room no entrega la primera lectura.
 * @param onEditarPerfil Navega al formulario de edición del perfil.
 * @param onRegistrarUsuario Devuelve al login con el aviso de registro; es el
 *   único camino que ofrece la vista invitado.
 * @param onCerrarSesion Termina la sesión y devuelve al login; la pantalla
 *   pide confirmación con un diálogo antes de ejecutarla.
 * @param onAbrirMenu Abre el menú lateral de navegación entre secciones.
 */
@Composable
fun MiPerfilScreen(
    usuario: Usuario?,
    estaCargando: Boolean,
    onEditarPerfil: () -> Unit,
    onRegistrarUsuario: () -> Unit,
    onCerrarSesion: () -> Unit,
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Visibilidad del diálogo de cierre de sesión. Con rememberSaveable el
    // diálogo sobrevive a rotaciones, y "Cancelar" o "Confirmar" lo ocultan.
    var dialogoCierreSesionVisible by rememberSaveable { mutableStateOf(false) }

    // Cerrar sesión es una acción con consecuencias: se confirma antes de
    // ejecutarla. El toque fuera del diálogo cuenta como "Cancelar".
    if (dialogoCierreSesionVisible) {
        DialogoConfirmacion(
            titulo = "Cerrar sesión",
            mensaje = "¿Seguro que quieres cerrar la sesión? Volverás a la " +
                "pantalla de inicio de sesión y deberás ingresar nuevamente " +
                "para ver tu perfil.",
            onConfirmar = {
                dialogoCierreSesionVisible = false
                onCerrarSesion()
            },
            onCancelar = { dialogoCierreSesionVisible = false }
        )
    }

    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            BarraSuperiorSeccion(
                titulo = "Mi perfil",
                onAbrirMenu = onAbrirMenu
            )
        }
    ) { paddingInterno ->
        if (estaCargando) {
            // Se evita el parpadeo de "Perfil sin configurar" mientras Room responde.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = DarkGreen)
            }
        } else if (usuario == null) {
            // Invitado: sin fila en Room ni sesión activa. La vista no expone
            // biografía ni cierre de sesión; solo el acceso al registro.
            VistaInvitado(
                onRegistrarUsuario = onRegistrarUsuario,
                modifier = Modifier.padding(paddingInterno)
            )
        } else {
            ContenidoPerfil(
                usuario = usuario,
                onEditarPerfil = onEditarPerfil,
                onSolicitarCierreSesion = { dialogoCierreSesionVisible = true },
                modifier = Modifier.padding(paddingInterno)
            )
        }
    }
}

/**
 * Vista del perfil para invitados: sin datos propios ni sesión que cerrar.
 *
 * Reemplaza por completo la vista registrada: no muestra biografía ni botón
 * de cierre de sesión, y ofrece un único camino principal ("Registrar
 * usuario") que devuelve al login con el aviso de registro.
 */
@Composable
private fun VistaInvitado(
    onRegistrarUsuario: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Filled.PersonOutline,
            contentDescription = null,
            tint = DarkGreen,
            modifier = Modifier.size(size = 72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Estás navegando como invitado",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Registra un usuario para guardar tu foto, tu nombre y tu " +
                "biografía, y poder cerrar sesión cuando quieras.",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onRegistrarUsuario,
            colors = ButtonDefaults.buttonColors(containerColor = YellowDuoc),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Registrar usuario",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Contenido scrolleable del perfil de un usuario registrado.
 *
 * Se separa como composable privado para mantener legible la pantalla y
 * porque no conoce navegación ni estado: solo pinta lo que recibe.
 *
 * @param onSolicitarCierreSesion Pide confirmación para cerrar la sesión; la
 *   pantalla dueña del diálogo decide qué hacer con la solicitud.
 */
@Composable
private fun ContenidoPerfil(
    usuario: Usuario,
    onEditarPerfil: () -> Unit,
    onSolicitarCierreSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nombreVisible = usuario.nombreCompleto.trim()
    val correoVisible = usuario.correoElectronico.trim()
    val biografiaVisible = usuario.biografia.trim()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FotoPerfil(
            uriFotoPerfil = usuario.uriFotoPerfil,
            nombreCompleto = usuario.nombreCompleto
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = nombreVisible.ifBlank { "Perfil sin configurar" },
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = correoVisible.ifBlank { "Aún no has registrado tus datos" },
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(all = 16.dp)) {
                Text(
                    text = "Biografía",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = biografiaVisible.ifBlank { "Sin biografía registrada." },
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onEditarPerfil,
            colors = ButtonDefaults.buttonColors(containerColor = YellowDuoc),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Editar perfil",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Acción secundaria y con consecuencias: va en OutlinedButton para no
        // competir con la acción principal y abre el diálogo de confirmación
        // en lugar de cerrar la sesión de inmediato.
        OutlinedButton(
            onClick = onSolicitarCierreSesion,
            colors = ButtonDefaults.outlinedButtonColors(contentColor = DarkGreen),
            border = BorderStroke(width = 1.dp, color = DarkGreen),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(
                text = "Cerrar sesión",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
