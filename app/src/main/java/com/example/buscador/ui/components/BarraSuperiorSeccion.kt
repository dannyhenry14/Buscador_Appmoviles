package com.example.buscador.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.buscador.ui.theme.DarkGreen

/**
 * Barra superior compartida por las secciones de nivel superior.
 *
 * Sustituye a los antiguos TextButton de navegación ("Nosotros", "Mi perfil"):
 * ahora la navegación entre secciones vive en el menú lateral y cada pantalla
 * principal solo expone el botón hamburguesa que lo abre.
 *
 * Mantiene la identidad visual del proyecto: fondo DarkGreen y contenido
 * blanco, igual que las barras con BotonVolver de las pantallas de detalle.
 *
 * @param titulo Texto central de la barra (nombre de la sección visible).
 * @param onAbrirMenu Invocado al presionar el botón hamburguesa; el NavHost
 *   resuelve la apertura real del drawer para que la vista no lo conozca.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperiorSeccion(
    titulo: String,
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = {
            Text(text = titulo, fontWeight = FontWeight.Bold)
        },
        navigationIcon = {
            IconButton(onClick = onAbrirMenu) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = "Abrir menú de navegación",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkGreen,
            titleContentColor = Color.White
        ),
        modifier = modifier
    )
}
