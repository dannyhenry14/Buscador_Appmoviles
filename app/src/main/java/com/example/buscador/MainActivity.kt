package com.example.buscador

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.buscador.navigation.BuscadorNavHost
import com.example.buscador.ui.theme.BuscadorTheme

/**
 * Única Activity de la aplicación (Single-Activity Architecture).
 *
 * No contiene pantallas propias: delega todo el contenido a [BuscadorNavHost],
 * que resuelve el grafo de navegación. Cada pantalla maneja sus propios insets
 * mediante Scaffold, por eso aquí solo se aplica fillMaxSize.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BuscadorTheme {
                BuscadorNavHost(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
