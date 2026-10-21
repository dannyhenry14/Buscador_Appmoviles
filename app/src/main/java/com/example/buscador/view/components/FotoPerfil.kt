package com.example.buscador.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.example.buscador.ui.theme.LightGrayMap

/**
 * Foto de perfil circular reutilizada por MiPerfil y EditarPerfil.
 *
 * - Si no hay URI guardada, muestra un marcador neutro en lugar de una imagen rota.
 * - Si hay URI, Coil resuelve la imagen (soporta content:// del selector de fotos).
 *
 * @param uriFotoPerfil URI persistida en Room, o null si el usuario no tiene foto.
 * @param nombreCompleto Nombre usado para el texto alternativo (accesibilidad).
 */
@Composable
fun FotoPerfil(
    uriFotoPerfil: String?,
    nombreCompleto: String?,
    modifier: Modifier = Modifier
) {
    val modificadorBase = modifier
        .size(128.dp)
        .clip(CircleShape)
        .background(LightGrayMap)

    if (uriFotoPerfil.isNullOrBlank()) {
        Box(
            modifier = modificadorBase,
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Sin foto", color = Color.Gray, fontSize = 12.sp)
        }
    } else {
        AsyncImage(
            model = uriFotoPerfil,
            contentDescription = "Foto de perfil de ${nombreCompleto ?: "usuario"}",
            contentScale = ContentScale.Crop,
            modifier = modificadorBase
        )
    }
}
