package com.example.buscador.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.R
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.GrisTextoSecundario
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.ui.components.BarraSuperiorSeccion

/**
 * Pantalla institucional "Sobre nosotros".
 *
 * Es una sección de nivel superior: se llega desde el menú lateral, por lo
 * que su barra expone el botón hamburguesa en lugar de "Volver". El contenido
 * es estático descriptivo de CENAMAD; si el texto crece (equipo, convenios),
 * conviene moverlo a strings.xml o a un recurso remoto.
 *
 * La fotografía se carga con `painterResource` porque es un drawable local
 * empaquetado en el APK (no requiere red ni carga asíncrona).
 *
 * @param onAbrirMenu Abre el menú lateral de navegación entre secciones.
 */
@Composable
fun SobreNosotrosScreen(
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            BarraSuperiorSeccion(
                titulo = "Sobre nosotros",
                onAbrirMenu = onAbrirMenu
            )
        }
    ) { paddingInterno ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingInterno)
                .verticalScroll(rememberScrollState())
                .padding(all = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Text(
                text = "CENAMAD",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )
            Text(
                text = "El Centro Nacional de Excelencia para la Industria de la " +
                        "Madera es una iniciativa conjunta de Duoc UC y el CITT que " +
                        "impulsa la investigación aplicada, la transferencia tecnológica " +
                        "y la formación de profesionales en construcción en madera. " +
                        "Desde nuestra sede San Joaquín articulamos universidades, " +
                        "empresas y gremios para consolidar la madera como material " +
                        "estructural competitivo y sustentable en Chile.",
                fontSize = 15.sp,
                color = Color.Black
            )

            // Fotografía ilustrativa de las instalaciones del centro.
            Image(
                painter = painterResource(id = R.drawable.img_laboratorio),
                contentDescription = "Laboratorios de investigación de CENAMAD",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(percent = 12))
            )
            Text(
                text = "Nuestros laboratorios en la sede San Joaquín.",
                fontSize = 12.sp,
                color = GrisTextoSecundario
            )

            Spacer(modifier = Modifier.height(8.dp))
            TituloSeccion(texto = "Nuestra misión")
            Text(
                text = "Generar y transferir conocimiento que permita posicionar la madera " +
                        "como material estructural competitivo y sustentable en Chile.",
                fontSize = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))
            TituloSeccion(texto = "Qué encontrarás en la app")
            Text(
                text = "• Agenda de seminarios, talleres y ferias del centro.\n" +
                        "• Detalle de cada actividad con fecha y ubicación.\n" +
                        "• Mapa del campus con las ubicaciones del centro.\n" +
                        "• Perfil personal con datos de contacto y fotografía.",
                fontSize = 15.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))
            TituloSeccion(texto = "Contacto")
            Text(
                text = "Correo: contacto@cenamad.cl\n" +
                        "Sitio web: www.cenamad.cl\n" +
                        "Sede: Av. Vicuña Mackenna 4860, San Joaquín.",
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}

/** Encabezado interno reutilizado para separar secciones de texto. */
@Composable
private fun TituloSeccion(texto: String) {
    Text(
        text = texto,
        fontSize = 18.sp,
        fontWeight = FontWeight.Bold,
        color = DarkGreen
    )
}