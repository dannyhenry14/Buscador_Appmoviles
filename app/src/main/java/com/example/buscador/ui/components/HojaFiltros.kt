package com.example.buscador.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.model.CategoriaEvento
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.NegroTexto
import com.example.buscador.ui.theme.White
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** Cantidad de milisegundos que tiene un día; se usa para obtener el último día de un mes. */
private const val MILISEGUNDOS_POR_DIA = 86_400_000L

/** Nombres de los meses en el orden del calendario (índice 0 = enero). */
private val nombresMeses = listOf(
    "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
    "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
)

/**
 * Hoja inferior con los filtros de la búsqueda de eventos: categoría (interés)
 * y fechas, ya sea por año y mes o por un rango exacto en el calendario.
 *
 * Los cambios se hacen sobre copias locales y solo se envían al tocar "Aplicar";
 * así el listado no se recarga mientras el usuario todavía está eligiendo.
 *
 * @param categoriaInicial Categoría que aparece marcada al abrir; null si no hay.
 * @param desdeInicialMillis Inicio del rango de fechas al abrir; null si no hay.
 * @param hastaInicialMillis Fin del rango de fechas al abrir; null si no hay.
 * @param onAplicar Se llama al tocar "Aplicar" con la categoría y las fechas elegidas.
 * @param onCerrar Se llama cuando la hoja debe cerrarse (deslizar hacia abajo o tocar fuera).
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HojaFiltros(
    categoriaInicial: CategoriaEvento?,
    desdeInicialMillis: Long?,
    hastaInicialMillis: Long?,
    onAplicar: (categoria: CategoriaEvento?, desdeMillis: Long?, hastaMillis: Long?) -> Unit,
    onCerrar: () -> Unit
) {
    // Copias locales: lo que el usuario elige aquí no cambia nada hasta tocar "Aplicar".
    var categoriaElegida by remember { mutableStateOf(categoriaInicial) }
    var desdeElegidoMillis by remember { mutableStateOf(desdeInicialMillis) }
    var hastaElegidoMillis by remember { mutableStateOf(hastaInicialMillis) }
    var mostrarCalendario by remember { mutableStateOf(value = false) }

    // Año y mes elegidos en los selectores (mes: 0 = enero ... 11 = diciembre).
    var anioElegido by remember { mutableStateOf<Int?>(null) }
    var mesElegido by remember { mutableStateOf<Int?>(null) }

    // Años que se ofrecen en el selector: el año actual y los dos siguientes.
    val anioActual = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val aniosDisponibles = listOf(anioActual, anioActual + 1, anioActual + 2)

    /**
     * Guarda el año y el mes elegidos y calcula el rango de fechas que les corresponde.
     * Sin año no hay rango; con año y sin mes, el rango es el año completo.
     */
    fun elegirAnioYMes(anioNuevo: Int?, mesNuevo: Int?) {
        anioElegido = anioNuevo
        mesElegido = if (anioNuevo == null) null else mesNuevo
        if (anioNuevo == null) {
            desdeElegidoMillis = null
            hastaElegidoMillis = null
        } else {
            val (desdeCalculado, hastaCalculado) = calcularRangoMillis(anioNuevo, mesNuevo)
            desdeElegidoMillis = desdeCalculado
            hastaElegidoMillis = hastaCalculado
        }
    }

    if (mostrarCalendario) {
        DialogoRangoFechas(
            desdeInicialMillis = desdeElegidoMillis,
            hastaInicialMillis = hastaElegidoMillis,
            onConfirmar = { desdeNuevo, hastaNuevo ->
                // Un rango exacto reemplaza lo elegido en los selectores de año y mes.
                anioElegido = null
                mesElegido = null
                desdeElegidoMillis = desdeNuevo
                hastaElegidoMillis = hastaNuevo
                mostrarCalendario = false
            },
            onCancelar = { mostrarCalendario = false }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onCerrar,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
        ) {
            Text(
                text = "Filtros",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Interés",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = NegroTexto
            )
            Spacer(modifier = Modifier.height(8.dp))
            // FlowRow acomoda los chips en varias líneas si no caben en una sola.
            FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                CategoriaEvento.entries.forEach { categoria ->
                    val estaSeleccionada = categoria == categoriaElegida
                    FilterChip(
                        selected = estaSeleccionada,
                        onClick = { categoriaElegida = if (estaSeleccionada) null else categoria },
                        label = { Text(text = categoria.nombreVisible) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DarkGreen,
                            selectedLabelColor = White
                        )
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Fechas",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = NegroTexto
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Selectores de año y mes: la forma rápida de filtrar por un mes completo.
            Row(modifier = Modifier.fillMaxWidth()) {
                SelectorDesplegable(
                    textoMostrado = anioElegido?.toString() ?: "Año",
                    opciones = listOf("Todos") + aniosDisponibles.map { anio -> anio.toString() },
                    onOpcionElegida = { indice ->
                        if (indice == 0) {
                            elegirAnioYMes(anioNuevo = null, mesNuevo = null)
                        } else {
                            elegirAnioYMes(
                                anioNuevo = aniosDisponibles[indice - 1],
                                mesNuevo = mesElegido
                            )
                        }
                    },
                    modifier = Modifier.weight(weight = 1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                SelectorDesplegable(
                    textoMostrado = mesElegido?.let { mes -> nombresMeses[mes] } ?: "Mes",
                    opciones = listOf("Todo el año") + nombresMeses,
                    onOpcionElegida = { indice ->
                        // Si todavía no hay año, se usa el año actual para no dejar el mes sin efecto.
                        elegirAnioYMes(
                            anioNuevo = anioElegido ?: anioActual,
                            mesNuevo = if (indice == 0) null else indice - 1
                        )
                    },
                    modifier = Modifier.weight(weight = 1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            // Rango exacto: abre el calendario para elegir "desde" y "hasta" día por día.
            val hayFechasElegidas = desdeElegidoMillis != null || hastaElegidoMillis != null
            Row(verticalAlignment = Alignment.CenterVertically) {
                FilterChip(
                    selected = hayFechasElegidas,
                    onClick = { mostrarCalendario = true },
                    label = {
                        Text(
                            text = if (hayFechasElegidas) {
                                describirRangoFechas(desdeElegidoMillis, hastaElegidoMillis)
                            } else {
                                "Elegir rango exacto"
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.DateRange,
                            contentDescription = "Elegir fechas",
                            modifier = Modifier.size(size = FilterChipDefaults.IconSize)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DarkGreen,
                        selectedLabelColor = White,
                        selectedLeadingIconColor = White
                    )
                )
                if (hayFechasElegidas) {
                    TextButton(
                        onClick = { elegirAnioYMes(anioNuevo = null, mesNuevo = null) }
                    ) {
                        Text(text = "Quitar", color = DarkGreen)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        categoriaElegida = null
                        elegirAnioYMes(anioNuevo = null, mesNuevo = null)
                    },
                    modifier = Modifier.weight(weight = 1f)
                ) {
                    Text(text = "Limpiar", color = DarkGreen)
                }
                Button(
                    onClick = {
                        onAplicar(categoriaElegida, desdeElegidoMillis, hastaElegidoMillis)
                    },
                    modifier = Modifier.weight(weight = 1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = DarkGreen,
                        contentColor = White
                    )
                ) {
                    Text(text = "Aplicar")
                }
            }
        }
    }
}

/**
 * Botón que, al tocarlo, despliega una lista de opciones para elegir una.
 *
 * @param textoMostrado Texto que se ve en el botón (la opción elegida o un título como "Año").
 * @param opciones Textos de las opciones del menú, en orden.
 * @param onOpcionElegida Se llama con la posición (0, 1, 2...) de la opción tocada.
 * @param modifier Modificador aplicado al contenedor del botón.
 */
@Composable
private fun SelectorDesplegable(
    textoMostrado: String,
    opciones: List<String>,
    onOpcionElegida: (indice: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    // Interruptor del menú: true lo muestra, false lo oculta.
    var estaAbierto by remember { mutableStateOf(value = false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { estaAbierto = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = textoMostrado,
                color = DarkGreen,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(weight = 1f, fill = false)
            )
            Icon(
                imageVector = Icons.Filled.ArrowDropDown,
                contentDescription = null,
                tint = DarkGreen
            )
        }
        DropdownMenu(
            expanded = estaAbierto,
            onDismissRequest = { estaAbierto = false }
        ) {
            opciones.forEachIndexed { indice, opcion ->
                DropdownMenuItem(
                    text = { Text(text = opcion) },
                    onClick = {
                        estaAbierto = false
                        onOpcionElegida(indice)
                    }
                )
            }
        }
    }
}

/**
 * Calcula el primer y el último día de un mes (o de un año completo) en milisegundos UTC.
 * Los eventos guardan su fecha a las 00:00 UTC, así que el "hasta" es el 00:00 del
 * último día: de esa forma un evento de ese día entra en el rango.
 *
 * @param anio Año del rango.
 * @param mes Mes de 0 (enero) a 11 (diciembre); null para tomar el año completo.
 * @return Par con la fecha "desde" y la fecha "hasta", en ese orden.
 */
private fun calcularRangoMillis(anio: Int, mes: Int?): Pair<Long, Long> {
    val mesInicial = mes ?: Calendar.JANUARY
    // El mes 12 no existe: Calendar lo interpreta como enero del año siguiente.
    val mesSiguiente = if (mes == null) Calendar.DECEMBER + 1 else mes + 1
    val calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
    calendario.clear()
    calendario.set(anio, mesInicial, 1)
    val desdeMillis = calendario.timeInMillis
    calendario.set(anio, mesSiguiente, 1)
    val hastaMillis = calendario.timeInMillis - MILISEGUNDOS_POR_DIA
    return Pair(desdeMillis, hastaMillis)
}

/**
 * Convierte el rango elegido en un texto corto para mostrar en el chip de fechas.
 * Las fechas vienen en UTC, por eso el formateador también usa UTC; así el
 * día que se muestra es el mismo que eligió el usuario en el calendario.
 *
 * @return "Fechas" si no hay rango; "dd/MM/yyyy – dd/MM/yyyy" si hay ambos extremos;
 * "Desde ..." o "Hasta ..." si solo hay uno.
 */
fun describirRangoFechas(desdeMillis: Long?, hastaMillis: Long?): String {
    if (desdeMillis == null && hastaMillis == null) return "Fechas"
    val formatoFecha = SimpleDateFormat("dd/MM/yyyy", Locale.forLanguageTag("es-CL")).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    val textoDesde = desdeMillis?.let { milisegundos -> formatoFecha.format(Date(milisegundos)) }
    val textoHasta = hastaMillis?.let { milisegundos -> formatoFecha.format(Date(milisegundos)) }
    return when {
        textoDesde != null && textoHasta != null -> "$textoDesde – $textoHasta"
        textoDesde != null -> "Desde $textoDesde"
        else -> "Hasta $textoHasta"
    }
}