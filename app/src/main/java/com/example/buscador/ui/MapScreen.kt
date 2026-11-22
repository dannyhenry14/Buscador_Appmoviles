package com.example.buscador.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.ui.components.BarraSuperiorSeccion
import com.example.buscador.ui.components.MapaGoogleWebView
import com.example.buscador.ui.components.BotonAbrirGoogleMaps
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.GrisTextoSecundario
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.White

/**
 * Pantalla de Mapa usando un WebView embebido de Google Maps.
 */
@Composable
fun MapScreen(
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Sede San Joaquín por defecto
    val latitud = -33.4983
    val longitud = -70.6146

    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            BarraSuperiorSeccion(
                titulo = "Mapa",
                onAbrirMenu = onAbrirMenu
            )
        }
    ) { paddingInterno ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
        ) {
            // Mapa WebView a pantalla completa
            MapaGoogleWebView(
                latitud = latitud,
                longitud = longitud,
                zoom = 16,
                modifier = Modifier.fillMaxSize()
            )

            // Tarjeta inferior
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.padding(all = 16.dp)) {
                    Text(
                        text = "Sede San Joaquín",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Av. Vicuña Mackenna 4860",
                        fontSize = 14.sp,
                        color = GrisTextoSecundario
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    BotonAbrirGoogleMaps(
                        latitud = latitud,
                        longitud = longitud,
                        texto = "Cómo llegar"
                    )
                }
            }
        }
    }
}
