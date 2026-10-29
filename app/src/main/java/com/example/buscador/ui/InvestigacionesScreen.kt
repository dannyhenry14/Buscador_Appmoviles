
package com.example.buscador.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Datos de ejemplo para nuestra primera pantalla
private val investigaciones = listOf(
    "Construcción en madera",
    "Tecnología de materiales",
    "Sustentabilidad e innovación"
)

@Composable
fun InvestigacionesScreen() {

    var busqueda by remember { mutableStateOf("") }

    val resultados = investigaciones.filter {
        it.contains(busqueda, ignoreCase = true)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {

        Text(
            text = "Investigaciones CENAMAD",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = busqueda,
            onValueChange = { busqueda = it },
            label = { Text("Buscar investigaciones") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(20.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(resultados) { investigacion ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = investigacion,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "Explorar línea de investigación",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
