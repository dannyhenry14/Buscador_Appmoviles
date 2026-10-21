package com.example.buscador

import android.app.Application
import com.example.buscador.data.ContenedorAplicacion

/**
 * Application personalizada: punto de arranque de las dependencias globales.
 *
 * Se declara en AndroidManifest.xml (android:name=".BuscadorApplication") y
 * garantiza que el contenedor exista antes de que cualquier Activity o ViewModel
 * solicite un repositorio.
 */
class BuscadorApplication : Application() {

    /** Contenedor de dependencias manual, inicializado en [onCreate]. */
    lateinit var contenedorAplicacion: ContenedorAplicacion
        private set

    override fun onCreate() {
        super.onCreate()
        contenedorAplicacion = ContenedorAplicacion(contexto = applicationContext)
    }
}
