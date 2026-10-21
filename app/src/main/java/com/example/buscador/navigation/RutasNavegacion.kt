package com.example.buscador.navigation

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

    /** Pantalla inicial: autenticación del usuario. */
    data object Login : RutaAplicacion(ruta = "login")

    /** Home de la aplicación: listado de eventos de CENAMAD. */
    data object Eventos : RutaAplicacion(ruta = "eventos")

    /** Información institucional de CENAMAD. */
    data object SobreNosotros : RutaAplicacion(ruta = "sobre_nosotros")

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
}
