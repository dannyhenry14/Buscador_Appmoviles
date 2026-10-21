package com.example.buscador.view.nosotros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.LightGrayMap
import com.example.buscador.view.components.BotonVolver

/**
 * Pantalla institucional "Sobre nosotros".
 *
 * Contenido estático descriptivo de CENAMAD. Si el texto crece (misión, equipo,
 * convenios), conviene moverlo a strings.xml o a un recurso remoto.
 *
 * @param onVolver Regresa a la pantalla anterior del back stack.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SobreNosotrosScreen(
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = LightGrayMap,
        topBar = {
            TopAppBar(
                title = { Text(text = "Sobre nosotros") },
                navigationIcon = { BotonVolver(onVolver = onVolver) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkGreen,
                    titleContentColor = Color.White
                )
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
                text = "Centro Nacional de Excelencia para la Industria de la Madera. " +
                    "Impulsamos la investigación aplicada, la transferencia tecnológica " +
                    "y la formación de profesionales en construcción en madera.",
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            TituloSeccion(texto = "Nuestra misión")
            Text(
                text = "Generar y transferir conocimiento que permita posicionar la madera " +
                    "como material estructural competitivo y sustentable en Chile.",
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            TituloSeccion(texto = "Qué encontrarás en la app")
            Text(
                text = "• Agenda de seminarios, talleres y ferias del centro.\n" +
                    "• Detalle de cada actividad con fecha y ubicación.\n" +
                    "• Perfil personal con datos de contacto y fotografía.",
                fontSize = 15.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
            TituloSeccion(texto = "Contacto")
            Text(
                text = "Correo: contacto@cenamad.cl\nSitio web: www.cenamad.cl",
                fontSize = 15.sp
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
