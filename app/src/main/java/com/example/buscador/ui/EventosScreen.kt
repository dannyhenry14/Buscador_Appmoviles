package com.example.buscador.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.buscador.ui.components.HojaFiltros

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
 * Incluye una barra de búsqueda por texto y un botón de filtros que abre una
 * hoja inferior con la categoría (interés) y el rango de fechas. Los filtros
 * elegidos viven en el estado de la pantalla y cada cambio se informa mediante
 * [onBuscar], para que el ViewModel filtre el listado.
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
    var fechaDesdeMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaHastaMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    // Interruptor de la hoja de filtros: true la muestra, false la oculta.
    var mostrarFiltros by remember { mutableStateOf(value = false) }

    // El botón de filtros se pinta de verde cuando hay algún filtro activo.
    val hayFiltrosActivos = categoriaSeleccionada != null ||
            fechaDesdeMillis != null ||
            fechaHastaMillis != null

    // La hoja solo existe mientras mostrarFiltros sea verdadero.
    if (mostrarFiltros) {
        HojaFiltros(
            categoriaInicial = categoriaSeleccionada,
            desdeInicialMillis = fechaDesdeMillis,
            hastaInicialMillis = fechaHastaMillis,
            onAplicar = { categoriaNueva, desdeNuevo, hastaNuevo ->
                categoriaSeleccionada = categoriaNueva
                fechaDesdeMillis = desdeNuevo
                fechaHastaMillis = hastaNuevo
                mostrarFiltros = false
                onBuscar(textoBusqueda, categoriaNueva, desdeNuevo, hastaNuevo)
            },
            onCerrar = { mostrarFiltros = false }
        )
    }

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
        // Columna raíz: sostiene la fila de búsqueda arriba y el listado (o el
        // estado vacío) ocupando el espacio restante mediante [Modifier.weight].
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LightGrayMap)
                .padding(paddingInterno)
        ) {
            // Fila superior: barra de búsqueda a la izquierda y botón de filtros a la derecha.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BarraBusquedaVisual(
                    texto = textoBusqueda,
                    onTextoCambiado = { textoNuevo ->
                        textoBusqueda = textoNuevo
                        onBuscar(
                            textoNuevo,
                            categoriaSeleccionada,
                            fechaDesdeMillis,
                            fechaHastaMillis
                        )
                    },
                    modifier = Modifier.weight(weight = 1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                BotonFiltros(
                    hayFiltrosActivos = hayFiltrosActivos,
                    onClick = { mostrarFiltros = true }
                )
            }
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
 * Barra de búsqueda por texto de la sección de eventos.
 *
 * Eleva el texto escrito a través de [onTextoCambiado]; el filtrado lo hace el
 * repositorio. Se usa un OutlinedTextField redondeado sobre fondo blanco con
 * lupa en DarkGreen, coherente con la paleta de TarjetaEvento y la barra
 * superior de la sección.
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
 * Botón cuadrado con el ícono de filtros, ubicado junto a la barra de búsqueda.
 * Es blanco cuando no hay filtros y verde cuando hay al menos uno activo, para
 * que el usuario note de un vistazo que el listado está filtrado.
 *
 * @param hayFiltrosActivos Verdadero si hay categoría o fechas elegidas.
 * @param onClick Se llama al tocar el botón (abre la hoja de filtros).
 */
@Composable
private fun BotonFiltros(
    hayFiltrosActivos: Boolean,
    onClick: () -> Unit
) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(size = 56.dp),
        shape = RoundedCornerShape(size = 12.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = if (hayFiltrosActivos) DarkGreen else White,
            contentColor = if (hayFiltrosActivos) White else DarkGreen
        )
    ) {
        Icon(
            imageVector = Icons.Filled.FilterList,
            contentDescription = "Abrir filtros"
        )
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