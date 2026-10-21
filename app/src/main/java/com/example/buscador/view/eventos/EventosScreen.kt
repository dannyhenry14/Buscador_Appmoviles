package com.example.buscador.view.eventos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.Evento
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.view.components.BarraSuperiorSeccion

/**
 * Pantalla Home de la aplicación: listado de eventos de CENAMAD.
 *
 * Composable "tonto": no conoce ViewModel ni navegación, solo recibe datos
 * y callbacks. Así se puede previsualizar y testear de forma aislada.
 *
 * La navegación entre secciones vive en el menú lateral: esta pantalla solo
 * expone [onAbrirMenu] (botón hamburguesa) y el detalle de evento como
 * flujo interno de la sección.
 *
 * @param listaEventos Eventos observados desde [com.example.buscador.viewmodel.EventosViewModel].
 * @param onAbrirDetalleEvento Navega al detalle del evento seleccionado.
 * @param onAbrirMenu Abre el menú lateral de navegación entre secciones.
 */
@Composable
fun EventosScreen(
    listaEventos: List<Evento>,
    onAbrirDetalleEvento: (idEvento: Long) -> Unit,
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            BarraSuperiorSeccion(
                titulo = "Eventos",
                onAbrirMenu = onAbrirMenu
            )
        }
    ) { paddingInterno ->
        if (listaEventos.isEmpty()) {
            // Estado vacío explícito: la UI nunca debe quedar en blanco sin explicación.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No hay eventos disponibles por el momento.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightGrayMap)
                    .padding(paddingInterno),
                contentPadding = PaddingValues(all = 16.dp),
                verticalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                items(items = listaEventos, key = { evento -> evento.idEvento }) { evento ->
                    TarjetaEvento(
                        evento = evento,
                        onAbrirDetalleEvento = onAbrirDetalleEvento
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta de un evento del listado.
 * Se separa como composable privado para mantener legible la pantalla principal.
 */
@Composable
private fun TarjetaEvento(
    evento: Evento,
    onAbrirDetalleEvento: (idEvento: Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAbrirDetalleEvento(evento.idEvento) }
    ) {
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Text(
                text = evento.titulo,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${evento.fecha} · ${evento.ubicacion}",
                fontSize = 13.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = evento.descripcion,
                fontSize = 14.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
