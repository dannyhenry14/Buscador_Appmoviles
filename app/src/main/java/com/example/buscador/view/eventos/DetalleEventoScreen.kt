package com.example.buscador.view.eventos

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.Evento
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.YellowDuoc
import com.example.buscador.view.components.BotonVolver

/**
 * Detalle de un evento seleccionado en el Home.
 *
 * El evento llega resuelto (o null si el id no existe) desde el NavHost:
 * la pantalla solo lo presenta, sin acceder a repositorios.
 *
 * La cabecera muestra a sangre completa la imagen local asociada al evento
 * y los textos secundarios usan gris oscuro fijo para garantizar el contraste
 * sobre el fondo claro de la pantalla.
 *
 * @param evento Evento a mostrar; null cuando la ruta contiene un id inválido.
 * @param onVolver Regresa al listado de eventos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleEventoScreen(
    evento: Evento?,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            TopAppBar(
                title = { Text(text = "Detalle del evento") },
                navigationIcon = { BotonVolver(onVolver = onVolver) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingInterno ->
        if (evento == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontró el evento solicitado.",
                    color = Color.Gray
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingInterno)
                    .verticalScroll(rememberScrollState())
            ) {
                // Cabecera a sangre completa: ocupa todo el ancho de la pantalla
                // con proporción panorámica 16:9 y recorte centrado (Crop).
                if (evento.imagenResId != 0) {
                    Image(
                        painter = painterResource(id = evento.imagenResId),
                        contentDescription = "Imagen del evento ${evento.titulo}",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(ratio = 16f / 9f),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier.padding(all = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(space = 12.dp)
                ) {
                    Text(
                        text = evento.titulo,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Text(
                        text = "Fecha: ${evento.fecha}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        // Gris oscuro fijo: el color por defecto del tema puede ser
                        // claro y resultar ilegible sobre el fondo gris de la pantalla.
                        color = Color.DarkGray
                    )
                    Text(
                        text = "Lugar: ${evento.ubicacion}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Descripción",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Text(
                        text = evento.descripcion,
                        fontSize = 15.sp,
                        color = Color.DarkGray
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    // Acción de ejemplo: aquí se conectará la inscripción cuando exista backend.
                    Button(
                        onClick = { /* TODO: implementar inscripción al evento */ },
                        colors = ButtonDefaults.buttonColors(containerColor = YellowDuoc),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Inscribirme",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
