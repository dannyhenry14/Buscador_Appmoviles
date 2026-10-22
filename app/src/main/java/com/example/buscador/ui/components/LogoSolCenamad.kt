package com.example.buscador.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.DoradoCenamad

/**
 * Logo sol de CENAMAD dibujado 100 % en código (sin recursos drawable).
 *
 * Sol de rayos radiales redondeados con el centro vacío, alternando el color
 * primario y el secundario en los rayos pares e impares. Los radios se calculan
 * sobre la dimensión mínima para que escale sin deformarse.
 *
 * Se extrajo del login como componente compartido para reutilizarlo también
 * en el encabezado del menú lateral de navegación.
 *
 * @param tamano Diámetro total del sol (ancho y alto iguales).
 * @param colorPrimario Color de los rayos pares (por defecto el verde institucional).
 * @param colorSecundario Color de los rayos impares (por defecto el dorado CENAMAD).
 */
@Composable
fun LogoSolCenamad(
    tamano: Dp,
    colorPrimario: Color = DarkGreen,
    colorSecundario: Color = DoradoCenamad
) {
    Canvas(modifier = Modifier.size(tamano)) {
        val cantidadRayos = 16
        val radioInterior = size.minDimension * 0.30f
        val radioExterior = size.minDimension * 0.48f
        val grosorRayo = size.minDimension * 0.085f

        for (indiceRayo in 0 until cantidadRayos) {
            val anguloRadianes = Math.toRadians(indiceRayo * (360.0 / cantidadRayos))
            val desplazamientoHorizontal = kotlin.math.cos(anguloRadianes).toFloat()
            val desplazamientoVertical = kotlin.math.sin(anguloRadianes).toFloat()
            drawLine(
                color = if (indiceRayo % 2 == 0) colorPrimario else colorSecundario,
                start = Offset(
                    x = center.x + radioInterior * desplazamientoHorizontal,
                    y = center.y + radioInterior * desplazamientoVertical
                ),
                end = Offset(
                    x = center.x + radioExterior * desplazamientoHorizontal,
                    y = center.y + radioExterior * desplazamientoVertical
                ),
                strokeWidth = grosorRayo,
                cap = StrokeCap.Round
            )
        }
    }
}
