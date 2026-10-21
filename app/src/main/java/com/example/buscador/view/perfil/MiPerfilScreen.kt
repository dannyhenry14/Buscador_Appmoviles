package com.example.buscador.view.perfil

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.buscador.view.components.BotonVolver
import com.example.buscador.view.components.FotoPerfil

/**
 * Perfil del usuario autenticado.
 *
 * Es una pantalla de solo lectura: el estado llega desde
 * [com.example.buscador.viewmodel.UsuarioViewModel] a través del NavHost,
 * por lo que Room es la única fuente de verdad (unidirectional data flow).
 *
 * @param usuario Usuario observado en Room; null mientras carga o si no existe.
 * @param onEditarPerfil Navega al formulario de edición.
 * @param onVolver Regresa al listado de eventos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MiPerfilScreen(
    usuario: Usuario?,
    onEditarPerfil: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            TopAppBar(
                title = { Text(text = "Mi perfil") },
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FotoPerfil(
                uriFotoPerfil = usuario?.uriFotoPerfil,
                nombreCompleto = usuario?.nombreCompleto
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = usuario?.nombreCompleto?.ifBlank { "Perfil sin configurar" }
                    ?: "Perfil sin configurar",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = usuario?.correoElectronico ?: "Aún no has registrado tus datos",
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
                        text = usuario?.biografia?.ifBlank { "Sin biografía registrada." }
                            ?: "Sin biografía registrada.",
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
        }
    }
}
