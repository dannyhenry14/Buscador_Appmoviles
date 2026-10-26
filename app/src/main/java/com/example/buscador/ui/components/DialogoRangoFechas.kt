package com.example.buscador.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * Diálogo con un calendario para elegir un rango de fechas ("desde" y "hasta").
 *
 * Las fechas viajan como milisegundos desde 1970 en UTC, el mismo formato que
 * guarda [com.example.buscador.model.Evento.fechaMillis]; así se pueden comparar
 * directamente al filtrar.
 *
 * @param desdeInicialMillis Fecha "desde" que aparece marcada al abrir; null si no hay.
 * @param hastaInicialMillis Fecha "hasta" que aparece marcada al abrir; null si no hay.
 * @param onConfirmar Se llama al tocar "Aplicar" con el rango elegido (cualquiera puede ser null).
 * @param onCancelar Se llama al tocar "Cancelar" o al cerrar el diálogo.
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

    DatePickerDialog(
        onDismissRequest = onCancelar,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmar(
                        estadoSelector.selectedStartDateMillis,
                        estadoSelector.selectedEndDateMillis
                    )
                }
            ) {
                Text(text = "Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancelar) {
                Text(text = "Cancelar")
            }
        }
    ) {
        DateRangePicker(
            state = estadoSelector,
            modifier = Modifier.height(height = 500.dp)
        )
    }
}