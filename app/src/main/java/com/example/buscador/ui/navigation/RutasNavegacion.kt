package com.example.buscador.ui.navigation

import android.net.Uri

/**
 * Catálogo central de rutas de Navigation Compose.
 *
 * Se modela como `sealed class` con `data object` para que las rutas sean
 * constantes de compilación, evitando strings sueltos y errores de tipeo
 * entre el NavHost y las pantallas.
 *
 * @property ruta Patrón registrado en el NavHost. En DetalleEvento incluye
 * el placeholder `{idEvento}` que se reemplaza al navegar.
 */
sealed class RutaAplicacion(val ruta: String) {

    /**
     * Pantalla inicial: autenticación del usuario.
     *
     * Acepta el argumento opcional `aviso`: un mensaje contextual (por ejemplo,
     * las instrucciones para el invitado que quiere registrar una cuenta) que
     * reemplaza al aviso por defecto del login. Sin aviso, la ruta equivale a
     * "login" y la pantalla muestra igualmente su banner informativo.
     */
    data object Login : RutaAplicacion(ruta = "login?aviso={aviso}") {
        /** Nombre del argumento opcional que viaja en la ruta del login. */
        const val ARGUMENTO_AVISO = "aviso"

        /**
         * Texto literal que Navigation entrega cuando se abre el login sin rellenar el
         * argumento; no es un aviso real y la pantalla debe ignorarlo.
         */
        const val MARCADOR_AVISO_SIN_VALOR = "{aviso}"


        /** Aviso para el invitado que quiere registrar un usuario desde su perfil. */
        const val AVISO_REGISTRO_INVITADO =
            "Para registrar tu usuario, toca \"Crear cuenta\" y elige tus credenciales."

        /** Construye la ruta del login con un aviso codificado para la UI. */
        fun crearRutaConAviso(aviso: String): String =
            "login?aviso=${Uri.encode(aviso)}"
    }

    /** Formulario de alta de cuenta: nombre, correo y contraseña persistidos en Room. */
    data object Registro : RutaAplicacion(ruta = "registro")

    /** Home de la aplicación: listado de eventos de CENAMAD. */
    data object Eventos : RutaAplicacion(ruta = "eventos")

    /** Información institucional de CENAMAD. */
    data object SobreNosotros : RutaAplicacion(ruta = "sobre_nosotros")

    /** Mapa del campus con las ubicaciones del centro (simulado en Compose). */
    data object Mapa : RutaAplicacion(ruta = "mapa")

    /** Detalle de un evento; recibe el id como argumento de ruta. */
    data object DetalleEvento : RutaAplicacion(ruta = "detalle_evento/{idEvento}") {
        /** Nombre del argumento que viaja en el NavBackStackEntry. */
        const val ARGUMENTO_ID_EVENTO = "idEvento"

        /** Construye la ruta concreta para un evento. */
        fun crearRuta(idEvento: Long): String = "detalle_evento/$idEvento"
    }

    /** Perfil del usuario autenticado. */
    data object MiPerfil : RutaAplicacion(ruta = "mi_perfil")

    /** Formulario de edición del perfil. */
    data object EditarPerfil : RutaAplicacion(ruta = "editar_perfil")

    companion object {
        /**
         * Secciones de nivel superior: son las únicas rutas donde el menú
         * lateral está habilitado (botón hamburguesa y gesto de borde).
         * Las pantallas de detalle o edición quedan fuera a propósito:
         * flujos internos que se resuelven con "Volver".
         */
        val seccionesConMenu: List<RutaAplicacion> = listOf(
            Eventos, Mapa, SobreNosotros, MiPerfil
        )
    }
}
