package com.example.buscador.ui.components

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.buscador.ui.theme.DarkGreen

/**
 * Helper para obtener coordenadas según el nombre de la ubicación,
 * sin necesidad de un servicio de Geocoding.
 */
fun obtenerCoordenadasPorUbicacion(ubicacion: String): Pair<Double, Double> {
    return when {
        ubicacion.contains("CENAMAD", ignoreCase = true) || ubicacion.contains("San Joaquín", ignoreCase = true) -> 
            Pair(-33.4983, -70.6146)
        ubicacion.contains("Laboratorio", ignoreCase = true) -> 
            Pair(-33.5001, -70.6168)
        ubicacion.contains("Extensión", ignoreCase = true) -> 
            Pair(-33.4414, -70.6403)
        else -> 
            Pair(-33.4983, -70.6146) // Sede San Joaquín por defecto
    }
}

/**
 * Composable que envuelve un WebView cargando Google Maps embebido.
 * Gratuito y sin dependencias adicionales.
 *
 * @param latitud Latitud geográfica.
 * @param longitud Longitud geográfica.
 * @param zoom Nivel de zoom (ej. 16).
 * @param modifier Modificador para el tamaño y forma.
 */
@Composable
fun MapaGoogleWebView(
    latitud: Double,
    longitud: Double,
    zoom: Int = 16,
    modifier: Modifier = Modifier
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT, 
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                webViewClient = WebViewClient()
                webChromeClient = android.webkit.WebChromeClient()
            }
        },
        update = { webView ->
            val html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
                    <style>
                        * { margin: 0; padding: 0; box-sizing: border-box; }
                        html, body { width: 100vw; height: 100vh; overflow: hidden; }
                        iframe { width: 100vw; height: 100vh; border: 0; display: block; }
                    </style>
                </head>
                <body>
                    <iframe src="https://maps.google.com/maps?q=$latitud,$longitud&z=$zoom&output=embed" allowfullscreen></iframe>
                </body>
                </html>
            """.trimIndent()
            webView.loadDataWithBaseURL("https://www.google.com", html, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

/**
 * Botón genérico para lanzar el intent nativo de Google Maps.
 */
@Composable
fun BotonAbrirGoogleMaps(
    latitud: Double,
    longitud: Double,
    texto: String = "Abrir en Google Maps",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Button(
        onClick = {
            try {
                val uri = Uri.parse("geo:$latitud,$longitud?q=$latitud,$longitud")
                val intent = Intent(Intent.ACTION_VIEW, uri)
                context.startActivity(intent)
            } catch (_: Exception) {
                Toast.makeText(context, "No hay aplicación de mapas instalada", Toast.LENGTH_SHORT).show()
            }
        },
        colors = ButtonDefaults.buttonColors(containerColor = DarkGreen),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(text = texto, color = androidx.compose.ui.graphics.Color.White)
    }
}
