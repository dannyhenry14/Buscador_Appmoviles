package com.example.buscador.view.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.AsyncImagePainter
import com.example.buscador.ui.theme.DarkGreen

/**
 * Foto de perfil circular reutilizada por MiPerfil y EditarPerfil.
 *
 * - Si no hay URI guardada, muestra un avatar con icono de usuario genérico en
 *   lugar de una imagen rota o de un texto plano.
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
        .size(size = 128.dp)
        .clip(CircleShape)
        // Fondo verde muy suave: el avatar se distingue del fondo gris de la
        // pantalla incluso cuando el usuario todavía no eligió una foto.
        .background(color = DarkGreen.copy(alpha = 0.12f))

    // Se reinicia al cambiar la URI: así una foto nueva no hereda el error de la anterior.
    var fotoIlegible by remember(uriFotoPerfil) { mutableStateOf(false) }

    Box(
        modifier = modificadorBase,
        contentAlignment = Alignment.Center
    ) {
        if (uriFotoPerfil.isNullOrBlank() || fotoIlegible) {
            // Avatar genérico: icono grande y centrado en el color institucional
            // en lugar de un texto, para que el marcador se vea intencional.
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = "Sin foto de perfil",
                tint = DarkGreen.copy(alpha = 0.55f),
                modifier = Modifier.size(size = 64.dp)
            )
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
