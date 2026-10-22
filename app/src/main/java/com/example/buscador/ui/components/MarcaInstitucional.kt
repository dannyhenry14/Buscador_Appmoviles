package com.example.buscador.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit

/**
 * Marca compuesta "Duoc UC | CITT" con colores corporativos configurables.
 *
 * Se extrajo del login como componente compartido: el login la usa en el pie
 * de pantalla y el menú lateral la repite en su pie. Centralizarla garantiza
 * que ambas copias mantengan tipografía y separadores idénticos.
 *
 * @param tamanoTexto Tamaño de fuente aplicado a las cuatro piezas de texto.
 * @param colorDuoc Color de la palabra "Duoc".
 * @param colorUC Color de las siglas "UC".
 * @param colorSeparador Color del separador " | ".
 * @param colorCITT Color de la sigla "CITT".
 * @param espaciadoLetras Espaciado entre letras opcional (por defecto el del sistema).
 */
@Composable
fun MarcaInstitucional(
    tamanoTexto: TextUnit,
    colorDuoc: Color,
    colorUC: Color,
    colorSeparador: Color,
    colorCITT: Color,
    modifier: Modifier = Modifier,
    espaciadoLetras: TextUnit = TextUnit.Unspecified
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "Duoc",
            color = colorDuoc,
            fontSize = tamanoTexto,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = espaciadoLetras
        )
        Text(
            text = "UC",
            color = colorUC,
            fontSize = tamanoTexto,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = espaciadoLetras
        )
        Text(
            text = " | ",
            color = colorSeparador,
            fontSize = tamanoTexto,
            fontWeight = FontWeight.Light
        )
        Text(
            text = "CITT",
            color = colorCITT,
            fontSize = tamanoTexto,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = espaciadoLetras
        )
    }
}
