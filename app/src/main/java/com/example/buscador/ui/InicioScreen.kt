package com.example.buscador.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.ClimaActual
import com.example.buscador.model.Evento
import com.example.buscador.ui.components.BarraSuperiorSeccion
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.DoradoCenamad
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.White

/**
 * Pantalla inicial (Dashboard) de la aplicación.
 *
 * Muestra un banner de bienvenida, la tarjeta del clima actual,
 * accesos rápidos a otras secciones y un resumen de los próximos eventos.
 */
@Composable
fun InicioScreen(
    climaActual: ClimaActual?,
    eventosDestacados: List<Evento>,
    errorCarga: String?,
    onReintentarCarga: () -> Unit,
    onAbrirMenu: () -> Unit,
    onIrAEventos: () -> Unit,
    onIrAMapa: () -> Unit,
    onIrASobreNosotros: () -> Unit,
    onAbrirDetalleEvento: (idEvento: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            BarraSuperiorSeccion(
                titulo = "Inicio",
                onAbrirMenu = onAbrirMenu
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Banner de Bienvenida
            Text(
                text = "Bienvenido a CENAMAD",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = DarkGreen
            )
            Text(
                text = "Centro Nacional de Excelencia para la Industria de la Madera. Descubre nuestras actividades, ubicaciones y novedades.",
                fontSize = 15.sp,
                color = DarkGreen.copy(alpha = 0.8f)
            )

            // Tarjeta de Clima
            if (climaActual != null) {
                TarjetaClimaDashboard(clima = climaActual)
            }

            // Accesos rápidos
            Text(
                text = "Accesos rápidos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(top = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BotonAccesoRapido(
                    titulo = "Eventos",
                    icono = Icons.Filled.DateRange,
                    onClick = onIrAEventos,
                    modifier = Modifier.weight(1f)
                )
                BotonAccesoRapido(
                    titulo = "Mapa",
                    icono = Icons.Filled.Place,
                    onClick = onIrAMapa,
                    modifier = Modifier.weight(1f)
                )
                BotonAccesoRapido(
                    titulo = "Nosotros",
                    icono = Icons.Filled.Info,
                    onClick = onIrASobreNosotros,
                    modifier = Modifier.weight(1f)
                )
            }

            // Próximos eventos
            Text(
                text = "Próximos eventos y novedades",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(top = 8.dp)
            )
            
            if (errorCarga != null) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Ocurrió un error al conectar con el servidor.", color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onReintentarCarga,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen)
                    ) {
                        Text("Reintentar", color = White)
                    }
                }
            } else if (eventosDestacados.isEmpty()) {
                Text(
                    text = "No hay eventos destacados por el momento.",
                    color = DarkGreen.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                eventosDestacados.forEach { evento ->
                    TarjetaEvento(
                        evento = evento,
                        onAbrirDetalleEvento = onAbrirDetalleEvento
                    )
                }
            }

            Button(
                onClick = onIrAEventos,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DoradoCenamad)
            ) {
                Text(text = "Ver todos los eventos", color = White, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Tarjeta compacta que muestra la información meteorológica actual.
 */
@Composable
private fun TarjetaClimaDashboard(
    clima: ClimaActual,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkGreen)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Santiago, Chile",
                    color = White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${clima.temperatura}°C",
                    color = White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = clima.condicion,
                    color = White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Viento: ${clima.viento} km/h",
                    color = White.copy(alpha = 0.8f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Botón cuadrado para acceso rápido a otras secciones.
 */
@Composable
private fun BotonAccesoRapido(
    titulo: String,
    icono: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(90.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icono,
                contentDescription = titulo,
                tint = DarkGreen,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = titulo,
                color = DarkGreen,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
