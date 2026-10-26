package com.example.buscador.ui

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.CategoriaEvento
import com.example.buscador.model.Evento
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.NegroTexto
import com.example.buscador.ui.theme.White
import com.example.buscador.ui.components.BarraSuperiorSeccion

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
 * Incluye una barra de búsqueda por texto y una fila de categorías (intereses).
 * Los filtros elegidos viven en el estado de la pantalla y cada cambio se
 * informa mediante [onBuscar], para que el ViewModel filtre el listado.
 *
 * @param listaEventos Eventos observados desde [com.example.buscador.viewmodel.EventosViewModel].
 * @param onAbrirDetalleEvento Navega al detalle del evento seleccionado.
 * @param onAbrirMenu Abre el menú lateral de navegación entre secciones.
 * @param onBuscar Avisa que cambió algún filtro; entrega texto, categoría y rango de fechas.
 */
@Composable
fun EventosScreen(
    listaEventos: List<Evento>,
    onAbrirDetalleEvento: (idEvento: Long) -> Unit,
    onAbrirMenu: () -> Unit,
    onBuscar: (String, CategoriaEvento?, Long?, Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    // Filtros elegidos por el usuario. Se usa rememberSaveable (y no remember)
    // para que sobrevivan al abrir el detalle de un evento y volver atrás: el
    // ViewModel conserva la lista filtrada y la pantalla debe mostrar los mismos filtros.
    var textoBusqueda by rememberSaveable { mutableStateOf(value = "") }
    var categoriaSeleccionada by rememberSaveable { mutableStateOf<CategoriaEvento?>(null) }

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
        // Columna raíz: sostiene la barra de búsqueda y los filtros arriba, y el
        // listado (o el estado vacío) ocupando el espacio restante mediante [Modifier.weight].
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGrayMap)
                .padding(paddingInterno)
        ) {
            // Barra de búsqueda anclada bajo la barra superior.
            BarraBusquedaVisual(
                texto = textoBusqueda,
                onTextoCambiado = { textoNuevo ->
                    textoBusqueda = textoNuevo
                    onBuscar(textoNuevo, categoriaSeleccionada, null, null)
                },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Fila de categorías (intereses) para filtrar por tipo de evento.
            FilaFiltrosCategoria(
                categoriaSeleccionada = categoriaSeleccionada,
                onCategoriaSeleccionada = { categoriaNueva ->
                    categoriaSeleccionada = categoriaNueva
                    onBuscar(textoBusqueda, categoriaNueva, null, null)
                }
            )
            if (listaEventos.isEmpty()) {
                // Estado vacío explícito: la UI nunca debe quedar en blanco sin explicación.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(weight = 1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "No se encontraron eventos con esos filtros.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(weight = 1f),
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
}

/**
 * Barra de búsqueda visual de la sección de eventos.
 *
 * Componente puramente de UI para el MVP: eleva el texto escrito a través de
 * [onTextoCambiado] pero todavía no filtra el listado ni consulta Room. Se usa
 * un OutlinedTextField redondeado sobre fondo blanco con lupa en DarkGreen,
 * coherente con la paleta de TarjetaEvento y la barra superior de la sección.
 *
 * @param texto Valor actual del campo de búsqueda.
 * @param onTextoCambiado Callback invocado con el nuevo texto al escribir.
 * @param modifier Modificador aplicado al contenedor del campo.
 */
@Composable
private fun BarraBusquedaVisual(
    texto: String,
    onTextoCambiado: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = texto,
        onValueChange = onTextoCambiado,
        modifier = modifier.fillMaxWidth(),
        placeholder = {
            Text(text = "Buscar eventos...", fontSize = 14.sp, color = Color.Gray)
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Buscar evento",
                tint = DarkGreen
            )
        },
        singleLine = true,
        shape = RoundedCornerShape(size = 12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = White,
            unfocusedContainerColor = White,
            focusedTextColor = NegroTexto,
            unfocusedTextColor = NegroTexto,
            cursorColor = DarkGreen,
            focusedBorderColor = DarkGreen,
            unfocusedBorderColor = Color.LightGray
        )
    )
}

/**
 * Fila horizontal de chips, uno por cada [CategoriaEvento].
 *
 * Funciona como interruptor: tocar un chip lo selecciona; tocar el chip ya
 * seleccionado lo desmarca y vuelve a mostrar todas las categorías (null).
 *
 * @param categoriaSeleccionada Categoría activa, o null si no hay ninguna.
 * @param onCategoriaSeleccionada Callback con la nueva categoría (o null al desmarcar).
 * @param modifier Modificador aplicado a la fila.
 */
@Composable
private fun FilaFiltrosCategoria(
    categoriaSeleccionada: CategoriaEvento?,
    onCategoriaSeleccionada: (CategoriaEvento?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        items(items = CategoriaEvento.entries, key = { categoria -> categoria.name }) { categoria ->
            val estaSeleccionada = categoria == categoriaSeleccionada
            FilterChip(
                selected = estaSeleccionada,
                onClick = {
                    onCategoriaSeleccionada(if (estaSeleccionada) null else categoria)
                },
                label = { Text(text = categoria.nombreVisible) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = DarkGreen,
                    selectedLabelColor = White
                )
            )
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
        Column {
            // Imagen a sangre completa en la cabecera de la tarjeta: el Surface
            // del Card recorta sus esquinas superiores con la forma redondeada.
            // Alto fijo + ContentScale.Crop para uniformidad en todo el listado.
            if (evento.imagenResId != 0) {
                Image(
                    painter = painterResource(id = evento.imagenResId),
                    contentDescription = "Imagen del evento ${evento.titulo}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 160.dp),
                    contentScale = ContentScale.Crop
                )
            }
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
}