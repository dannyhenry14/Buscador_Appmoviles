package com.example.buscador.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.GrisTextoSecundario

/**
 * Diálogo de confirmación reutilizable construido sobre el `AlertDialog`
 * de Material 3.
 *
 * Contrato de estado: quien lo invoca decide cuándo mostrarlo (un estado
 * visible/invisible en la pantalla) y este composable no guarda nada propio.
 * Ambos botones cierran el diálogo: "Confirmar" ejecuta [onConfirmar] y
 * "Cancelar" solo descarta la acción. El toque fuera del diálogo y la tecla
 * atrás se tratan como "Cancelar" ([onDismissRequest]) para no disparar
 * acciones sensibles por accidente.
 *
 * El título y el botón de confirmación usan el verde institucional y
 * "Cancelar" queda en gris secundario, de modo que la acción principal
 * destaque sin romper el estilo minimalista de la app.
 *
 * @param titulo Encabezado breve que nombra la acción a confirmar.
 * @param mensaje Explica la consecuencia de confirmar, en lenguaje directo.
 * @param onConfirmar Acción a ejecutar cuando el usuario acepta.
 * @param onCancelar Acción a ejecutar al cancelar (botón, toque fuera o atrás).
 */
@Composable
fun DialogoConfirmacion(
    titulo: String,
    mensaje: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = {
            Text(
                text = titulo,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
        },
        text = {
            Text(text = mensaje)
        },
        confirmButton = {
            TextButton(
                onClick = onConfirmar,
                colors = ButtonDefaults.textButtonColors(contentColor = DarkGreen)
            ) {
                Text(
                    text = "Confirmar",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onCancelar,
                colors = ButtonDefaults.textButtonColors(contentColor = GrisTextoSecundario)
            ) {
                Text(text = "Cancelar")
            }
        },
        modifier = modifier
    )
}
