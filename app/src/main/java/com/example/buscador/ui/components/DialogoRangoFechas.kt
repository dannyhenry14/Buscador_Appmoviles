package com.example.buscador.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.NegroTexto
import com.example.buscador.ui.theme.White

/**
 * Diálogo a pantalla completa con un calendario para elegir un rango de fechas
 * ("desde" y "hasta").
 *
 * Las fechas viajan como milisegundos desde 1970 en UTC, el mismo formato que
 * guarda [com.example.buscador.model.Evento.fechaMillis]; así se pueden comparar
 * directamente al filtrar.
 *
 * @param desdeInicialMillis Fecha "desde" que aparece marcada al abrir; null si no hay.
 * @param hastaInicialMillis Fecha "hasta" que aparece marcada al abrir; null si no hay.
 * @param onConfirmar Se llama al tocar "Aplicar" con el rango elegido (cualquiera puede ser null).
 * @param onCancelar Se llama al tocar la X o al cerrar el diálogo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DialogoRangoFechas(
    desdeInicialMillis: Long?,
    hastaInicialMillis: Long?,
    onConfirmar: (desdeMillis: Long?, hastaMillis: Long?) -> Unit,
    onCancelar: () -> Unit
) {
    // Estado interno del calendario: recuerda qué días va eligiendo el usuario.
    val estadoSelector = rememberDateRangePickerState(
        initialSelectedStartDateMillis = desdeInicialMillis,
        initialSelectedEndDateMillis = hastaInicialMillis
    )

    // Texto que resume lo elegido hasta ahora, se actualiza al tocar los días.
    val textoRangoElegido = if (
        estadoSelector.selectedStartDateMillis == null &&
        estadoSelector.selectedEndDateMillis == null
    ) {
        "Inicio – Fin"
    } else {
        describirRangoFechas(
            desdeMillis = estadoSelector.selectedStartDateMillis,
            hastaMillis = estadoSelector.selectedEndDateMillis
        )
    }

    Dialog(
        onDismissRequest = onCancelar,
        // Sin esto el diálogo tendría el ancho reducido por defecto de Android.
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = White
        ) {
            Column(modifier = Modifier.systemBarsPadding()) {
                // Barra superior propia: cerrar a la izquierda, título al centro y Aplicar a la derecha.
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = onCancelar) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = "Cerrar calendario",
                            tint = DarkGreen
                        )
                    }
                    Text(
                        text = "Elige las fechas",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    TextButton(
                        onClick = {
                            onConfirmar(
                                estadoSelector.selectedStartDateMillis,
                                estadoSelector.selectedEndDateMillis
                            )
                        }
                    ) {
                        Text(text = "Aplicar", color = DarkGreen, fontWeight = FontWeight.Bold)
                    }
                }
                DateRangePicker(
                    state = estadoSelector,
                    modifier = Modifier.weight(weight = 1f),
                    title = null,
                    headline = {
                        Text(
                            text = textoRangoElegido,
                            modifier = Modifier.padding(start = 24.dp, bottom = 12.dp),
                            fontSize = 16.sp,
                            color = NegroTexto
                        )
                    },
                    showModeToggle = false,
                    colors = DatePickerDefaults.colors(
                        containerColor = White,
                        selectedDayContainerColor = DarkGreen,
                        selectedDayContentColor = White,
                        todayDateBorderColor = DarkGreen,
                        todayContentColor = DarkGreen,
                        dayInSelectionRangeContainerColor = DarkGreen.copy(alpha = 0.15f)
                    )
                )
            }
        }
    }
}