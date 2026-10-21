package com.example.buscador.view.perfil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.buscador.view.components.BarraSuperiorSeccion
import com.example.buscador.view.components.FotoPerfil

/**
 * Perfil del usuario autenticado.
 *
 * Es una pantalla de solo lectura: el usuario llega desde
 * [com.example.buscador.viewmodel.UsuarioViewModel] a través del NavHost.
 * No mantiene copia local de los datos, por lo que Room es la única fuente
 * de verdad y la vista se refresca sola cada vez que cambia una fila.
 *
 * Es una sección de nivel superior: se llega desde el menú lateral, por lo
 * que la barra expone el botón hamburguesa en lugar de "Volver".
 *
 * @param usuario Usuario observado en Room; null si todavía no existe.
 * @param estaCargando `true` mientras Room no entrega la primera lectura.
 * @param onEditarPerfil Navega al formulario de edición (o de creación).
 * @param onAbrirMenu Abre el menú lateral de navegación entre secciones.
 */
@Composable
fun MiPerfilScreen(
    usuario: Usuario?,
    estaCargando: Boolean,
    onEditarPerfil: () -> Unit,
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
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
        } else {
            ContenidoPerfil(
                usuario = usuario,
                onEditarPerfil = onEditarPerfil,
                modifier = Modifier.padding(paddingInterno)
            )
        }
    }
}

/**
 * Contenido scrolleable del perfil.
 *
 * Se separa como composable privado para mantener legible la pantalla y
 * porque no conoce navegación ni estado: solo pinta lo que recibe.
 */
@Composable
private fun ContenidoPerfil(
    usuario: Usuario?,
    onEditarPerfil: () -> Unit,
    modifier: Modifier = Modifier
) {
    val nombreVisible = usuario?.nombreCompleto?.trim().orEmpty()
    val correoVisible = usuario?.correoElectronico?.trim().orEmpty()
    val biografiaVisible = usuario?.biografia?.trim().orEmpty()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(all = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FotoPerfil(
            uriFotoPerfil = usuario?.uriFotoPerfil,
            nombreCompleto = usuario?.nombreCompleto
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
                text = if (usuario == null) "Crear perfil" else "Editar perfil",
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
