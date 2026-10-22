package com.example.buscador.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.GrisTextoSecundario
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.theme.NegroTexto
import com.example.buscador.ui.theme.White
import com.example.buscador.ui.theme.YellowDuoc
import com.example.buscador.ui.components.BarraSuperiorSeccion

/*
 * Paleta del mapa simulado.
 *
 * Estos colores imitan el estilo de un mapa (parques, calles, manzanas y
 * edificios) y no forman parte de la paleta institucional, por eso viven
 * acá y no en ui/theme/Color.kt.
 */
private val VerdeZonaParque = Color(0xFF9CC48F)
private val BordeCalle = Color(0xFFB9C0B3)
private val Calle = Color(0xFFFFFFFF)
private val EdificioBeige = Color(0xFFE8DCC3)
private val EdificioGris = Color(0xFFD9D9D6)
private val EdificioNaranja = Color(0xFFF2A65A)

/** Aviso mostrado por los controles flotantes mientras el mapa es un MVP. */
private const val MENSAJE_MAPA_EN_DESARROLLO = "Función de mapa en desarrollo"

/**
 * Ubicación señalada en el mapa del campus.
 *
 * Las posiciones se expresan como fracciones (0..1) del tamaño del lienzo,
 * no en píxeles: así los pines conservan su lugar relativo ante cualquier
 * densidad de pantalla o relación de aspecto.
 *
 * @param nombre Etiqueta visible bajo el pin.
 * @param fraccionX Posición horizontal respecto del ancho del lienzo.
 * @param fraccionY Posición vertical respecto del alto del lienzo.
 * @param colorPin Color de relleno del pin.
 * @param mostrarPin `false` dibuja solo la etiqueta (edificios de referencia
 *   como "Aulas" que en la maqueta aparecen nombrados sin marcador).
 */
private data class UbicacionEnMapa(
    val nombre: String,
    val fraccionX: Float,
    val fraccionY: Float,
    val colorPin: Color,
    val mostrarPin: Boolean = true
)

/** Ubicaciones de la sede San Joaquín mostradas en el mapa simulado. */
private val ubicacionesCampus = listOf(
    UbicacionEnMapa(
        nombre = "Campus",
        fraccionX = 0.31f,
        fraccionY = 0.18f,
        colorPin = DarkGreen
    ),
    UbicacionEnMapa(
        nombre = "Biblioteca",
        fraccionX = 0.25f,
        fraccionY = 0.35f,
        colorPin = DarkGreen
    ),
    UbicacionEnMapa(
        nombre = "CITT",
        fraccionX = 0.58f,
        fraccionY = 0.45f,
        colorPin = YellowDuoc
    ),
    UbicacionEnMapa(
        nombre = "Aulas",
        fraccionX = 0.41f,
        fraccionY = 0.61f,
        colorPin = DarkGreen,
        mostrarPin = false
    ),
    UbicacionEnMapa(
        nombre = "Laboratorios",
        fraccionX = 0.70f,
        fraccionY = 0.63f,
        colorPin = DarkGreen
    )
)

/**
 * Mapa del campus con las ubicaciones del centro.
 *
 * Es una simulación visual (no usa Google Maps ni geolocalización): el fondo
 * —manzanas, calles, avenida y edificios— se dibuja en un único [Canvas], y
 * los pines con sus etiquetas se componen encima anclados por fracciones.
 * La barra de búsqueda sigue siendo decorativa; los controles flotantes
 * muestran un aviso de función en desarrollo al pulsarlos, mientras el
 * mapa real no esté integrado.
 *
 * Es una sección de nivel superior: su barra expone el botón hamburguesa
 * del menú lateral.
 *
 * @param onAbrirMenu Abre el menú lateral de navegación entre secciones.
 */
@Composable
fun MapScreen(
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            // Capa base: fondo, zonas verdes, edificios, calles y avenida.
            Canvas(modifier = Modifier.fillMaxSize()) {
                dibujarZonasVerdes(ancho = size.width, alto = size.height)
                dibujarEdificios(ancho = size.width, alto = size.height)
                dibujarCalles(ancho = size.width, alto = size.height)
                dibujarAvenidaPrincipal(ancho = size.width, alto = size.height)
            }

            // Capa de pines: se dibuja sobre el fondo, en coordenadas relativas.
            ubicacionesCampus.forEach { ubicacion ->
                val alineacion = alineacionDesdeFraccion(
                    fraccionX = ubicacion.fraccionX,
                    fraccionY = ubicacion.fraccionY
                )
                if (ubicacion.mostrarPin) {
                    PinConEtiqueta(
                        ubicacion = ubicacion,
                        modifier = Modifier.align(alignment = alineacion)
                    )
                } else {
                    EtiquetaUbicacion(
                        texto = ubicacion.nombre,
                        modifier = Modifier.align(alignment = alineacion)
                    )
                }
            }

            // Superposiciones decorativas, igual que en la maqueta.
            BarraBusquedaMapa(
                modifier = Modifier
                    .align(alignment = Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .padding(horizontal = 24.dp)
            )
            ControlesFlotantesMapa(
                modifier = Modifier
                    .align(alignment = Alignment.CenterEnd)
                    .padding(end = 24.dp)
            )
            TarjetaInformacionSede(
                modifier = Modifier
                    .align(alignment = Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .padding(horizontal = 24.dp)
            )
        }
    }
}

/**
 * Convierte una fracción (0..1) del lienzo en una alineación con sesgo de Box.
 *
 * BiasAlignment trabaja en el rango [-1, 1] respecto del centro, por lo que
 * 0 (borde inicial) se traduce a -1 y 1 (borde final) a +1. Así un pin se
 * ancla exactamente donde corresponde dentro del lienzo sin medir en dp.
 */
private fun alineacionDesdeFraccion(fraccionX: Float, fraccionY: Float): Alignment =
    BiasAlignment(
        horizontalBias = fraccionX * 2f - 1f,
        verticalBias = fraccionY * 2f - 1f
    )

/** Pinta las áreas verdes tipo parque sobre el fondo del mapa. */
private fun DrawScope.dibujarZonasVerdes(ancho: Float, alto: Float) {
    drawRect(color = LightGrayMap)

    val zonasVerdes = listOf(
        Offset(x = ancho * 0.55f, y = alto * 0.04f) to Size(width = ancho * 0.45f, height = alto * 0.28f),
        Offset(x = ancho * 0.04f, y = alto * 0.44f) to Size(width = ancho * 0.24f, height = alto * 0.24f),
        Offset(x = ancho * 0.56f, y = alto * 0.74f) to Size(width = ancho * 0.44f, height = alto * 0.26f)
    )
    zonasVerdes.forEach { (origen, tamano) ->
        drawRoundRect(
            color = VerdeZonaParque.copy(alpha = 0.55f),
            topLeft = origen,
            size = tamano,
            cornerRadius = CornerRadius(x = 24f, y = 24f)
        )
    }
}

/** Pinta las manzanas institucionales (edificios de referencia del campus). */
private fun DrawScope.dibujarEdificios(ancho: Float, alto: Float) {
    val edificios = listOf(
        // Campus: bloque gris en el sector norte.
        Triple(Offset(x = ancho * 0.20f, y = alto * 0.10f), Size(width = ancho * 0.22f, height = alto * 0.10f), EdificioGris),
        // Biblioteca: bloque beige al poniente.
        Triple(Offset(x = ancho * 0.17f, y = alto * 0.30f), Size(width = ancho * 0.15f, height = alto * 0.10f), EdificioBeige),
        // CITT: bloque naranja junto a la avenida.
        Triple(Offset(x = ancho * 0.52f, y = alto * 0.41f), Size(width = ancho * 0.13f, height = alto * 0.09f), EdificioNaranja),
        // Aulas: bloque naranja extenso al sur.
        Triple(Offset(x = ancho * 0.30f, y = alto * 0.56f), Size(width = ancho * 0.22f, height = alto * 0.10f), EdificioNaranja),
        // Laboratorios: bloque beige al oriente.
        Triple(Offset(x = ancho * 0.60f, y = alto * 0.58f), Size(width = ancho * 0.20f, height = alto * 0.10f), EdificioBeige),
        // Anexo de laboratorios al sur del campus.
        Triple(Offset(x = ancho * 0.24f, y = alto * 0.76f), Size(width = ancho * 0.16f, height = alto * 0.08f), EdificioBeige)
    )
    edificios.forEach { (origen, tamano, color) ->
        drawRoundRect(
            color = color,
            topLeft = origen,
            size = tamano,
            cornerRadius = CornerRadius(x = 14f, y = 14f)
        )
    }
}

/**
 * Pinta las calles del campus.
 *
 * Cada calle se dibuja dos veces: primero un borde gris y encima la superficie
 * blanca, logrando el efecto de bordillos de la maqueta sin dibujar polígonos.
 */
private fun DrawScope.dibujarCalles(ancho: Float, alto: Float) {
    val grosorCalle = ancho * 0.022f

    val callesHorizontales = listOf(alto * 0.26f, alto * 0.52f, alto * 0.78f)
    val callesVerticales = listOf(ancho * 0.14f, ancho * 0.47f, ancho * 0.85f)

    callesHorizontales.forEach { posicionY ->
        val inicio = Offset(x = 0f, y = posicionY)
        val fin = Offset(x = ancho, y = posicionY)
        drawLine(color = BordeCalle, start = inicio, end = fin, strokeWidth = grosorCalle + 6f)
        drawLine(color = Calle, start = inicio, end = fin, strokeWidth = grosorCalle)
    }
    callesVerticales.forEach { posicionX ->
        val inicio = Offset(x = posicionX, y = 0f)
        val fin = Offset(x = posicionX, y = alto)
        drawLine(color = BordeCalle, start = inicio, end = fin, strokeWidth = grosorCalle + 6f)
        drawLine(color = Calle, start = inicio, end = fin, strokeWidth = grosorCalle)
    }
}

/** Pinta la avenida principal en naranja, eje visual de la maqueta. */
private fun DrawScope.dibujarAvenidaPrincipal(ancho: Float, alto: Float) {
    val inicio = Offset(x = ancho * 0.06f, y = 0f)
    val fin = Offset(x = ancho * 0.42f, y = alto)
    val grosorAvenida = ancho * 0.05f

    drawLine(
        color = White,
        start = inicio,
        end = fin,
        strokeWidth = grosorAvenida + 8f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = YellowDuoc,
        start = inicio,
        end = fin,
        strokeWidth = grosorAvenida,
        cap = StrokeCap.Round
    )
}

/**
 * Pin de ubicación con etiqueta, como los de la maqueta: gota con punta
 * inferior y centro hueco, más el nombre del lugar debajo.
 *
 * La forma de la gota se resuelve con un único Path: un óvalo para el cuerpo
 * y dos curvas Bézier cuadráticas que convergen en la punta inferior.
 */
@Composable
private fun PinConEtiqueta(
    ubicacion: UbicacionEnMapa,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(modifier = Modifier.size(width = 30.dp, height = 40.dp)) {
            val radioCuerpo = size.width * 0.42f
            val centroX = size.width / 2f
            val centroY = size.height * 0.34f

            val formaPin = Path().apply {
                addOval(
                    Rect(
                        left = centroX - radioCuerpo,
                        top = centroY - radioCuerpo,
                        right = centroX + radioCuerpo,
                        bottom = centroY + radioCuerpo
                    )
                )
                moveTo(x = centroX - radioCuerpo * 0.78f, y = centroY + radioCuerpo * 0.62f)
                quadraticTo(
                    x1 = centroX - radioCuerpo * 0.30f,
                    y1 = centroY + radioCuerpo * 1.25f,
                    x2 = centroX,
                    y2 = size.height
                )
                quadraticTo(
                    x1 = centroX + radioCuerpo * 0.30f,
                    y1 = centroY + radioCuerpo * 1.25f,
                    x2 = centroX + radioCuerpo * 0.78f,
                    y2 = centroY + radioCuerpo * 0.62f
                )
                close()
            }
            drawPath(path = formaPin, color = ubicacion.colorPin)
            drawCircle(
                color = White,
                radius = radioCuerpo * 0.45f,
                center = Offset(x = centroX, y = centroY)
            )
        }
        Text(
            text = ubicacion.nombre,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = NegroTexto
        )
    }
}

/**
 * Etiqueta de texto plano para edificios de referencia sin pin.
 * El fondo blanco semitransparente mantiene el texto legible sobre el mapa.
 */
@Composable
private fun EtiquetaUbicacion(
    texto: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = texto,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        color = NegroTexto,
        modifier = modifier
            .background(
                color = White.copy(alpha = 0.8f),
                shape = RoundedCornerShape(percent = 30)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

/** Barra de búsqueda superior, decorativa (sin lógica de filtrado aún). */
@Composable
private fun BarraBusquedaMapa(modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(percent = 50),
        color = White,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Buscar…",
                color = GrisTextoSecundario,
                fontSize = 15.sp,
                modifier = Modifier.weight(weight = 1f)
            )
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "Buscar en el mapa",
                tint = GrisTextoSecundario
            )
        }
    }
}

/**
 * Botones flotantes de georreferencia y zoom.
 *
 * Mientras el mapa es un MVP, los tres controles muestran un [Toast] de aviso
 * en lugar de ejecutar la acción real de geolocalización o zoom.
 */
@Composable
private fun ControlesFlotantesMapa(modifier: Modifier = Modifier) {
    val contexto = LocalContext.current

    // Acción temporal compartida por los tres controles mientras no exista
    // un motor de mapa real que ejecutar.
    val avisarFuncionEnDesarrollo = {
        Toast.makeText(
            contexto,
            MENSAJE_MAPA_EN_DESARROLLO,
            Toast.LENGTH_SHORT
        ).show()
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BotonCircularMapa(onClick = avisarFuncionEnDesarrollo) {
            Icon(
                imageVector = Icons.Filled.MyLocation,
                contentDescription = "Centrar en mi ubicación",
                tint = DarkGreen
            )
        }
        BotonCircularMapa(onClick = avisarFuncionEnDesarrollo) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Acercar el mapa",
                tint = DarkGreen
            )
        }
        BotonCircularMapa(onClick = avisarFuncionEnDesarrollo) {
            Icon(
                imageVector = Icons.Filled.Remove,
                contentDescription = "Alejar el mapa",
                tint = DarkGreen
            )
        }
    }
}

/**
 * Base circular blanca compartida por los controles flotantes del mapa.
 *
 * El contenido se centra con un [Box] porque la versión con `onClick` de
 * [Surface] no expone `contentAlignment`; así los iconos quedan exactamente
 * en el medio del círculo.
 */
@Composable
private fun BotonCircularMapa(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contenido: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(46.dp),
        shape = CircleShape,
        color = White,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            contenido()
        }
    }
}

/** Tarjeta inferior con la dirección de la sede, como en la maqueta. */
@Composable
private fun TarjetaInformacionSede(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
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
        }
    }
}
