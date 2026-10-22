package com.example.buscador.ui.components

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Botón de retroceso reutilizable para los `TopAppBar`.
 *
 * Se usa texto en lugar de un icono para no agregar la dependencia de
 * material-icons y mantener el estilo institucional de CENAMAD.
 * El contenido se pinta en blanco porque las barras superiores usan DarkGreen.
 */
@Composable
fun BotonVolver(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onVolver,
        modifier = modifier
    ) {
        Text(text = "← Volver", color = Color.White)
    }
}
