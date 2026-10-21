package com.example.buscador.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.buscador.ui.theme.LightGrayMap

/**
 * Foto de perfil circular reutilizada por MiPerfil y EditarPerfil.
 *
 * - Si no hay URI guardada, muestra un marcador neutro en lugar de una imagen rota.
 * - Si hay URI, Coil resuelve la imagen (soporta content:// del selector y de FileProvider).
 * - Si la URI ya no es legible (por ejemplo, una foto antigua cuyo permiso expiró),
 *   Coil emite error y se vuelve al marcador en lugar de dejar un círculo vacío.
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

    // Se reinicia al cambiar la URI: así una foto nueva no hereda el error de la anterior.
    var fotoIlegible by remember(uriFotoPerfil) { mutableStateOf(false) }

    Box(
        modifier = modificadorBase,
        contentAlignment = Alignment.Center
    ) {
        if (uriFotoPerfil.isNullOrBlank() || fotoIlegible) {
            Text(text = "Sin foto", color = Color.Gray, fontSize = 12.sp)
        } else {
            AsyncImage(
                model = uriFotoPerfil,
                contentDescription = "Foto de perfil de ${nombreCompleto ?: "usuario"}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onState = { estado ->
                    fotoIlegible = estado is AsyncImagePainter.State.Error
                }
            )
        }
    }
}
