package com.example.buscador.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.buscador.view.LoginScreen
import com.example.buscador.view.eventos.DetalleEventoScreen
import com.example.buscador.view.eventos.EventosScreen
import com.example.buscador.view.nosotros.SobreNosotrosScreen
import com.example.buscador.view.perfil.EditarPerfilScreen
import com.example.buscador.view.perfil.MiPerfilScreen
import com.example.buscador.viewmodel.EventosViewModel
import com.example.buscador.viewmodel.UsuarioViewModel

/**
 * Grafo de navegación único de la aplicación.
 *
 * Decisiones técnicas:
 * - Los ViewModel se crean a nivel de NavHost (estado compartido entre pantallas),
 *   no por pantalla, para que Eventos/Detalle y Perfil/EditarPerfil no dupliquen
 *   consultas ni pierdan estado al navegar.
 * - El estado se recolecta con `collectAsStateWithLifecycle` para detener la
 *   observación cuando la pantalla no está visible (ahorro de recursos).
 * - Login es el destino inicial; al ingresar se elimina del back stack con
 *   `popUpTo(...)` para que "Atrás" no regrese al formulario.
 */
@Composable
fun BuscadorNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    viewModelUsuario: UsuarioViewModel = viewModel(factory = UsuarioViewModel.Fabrica),
    viewModelEventos: EventosViewModel = viewModel(factory = EventosViewModel.Fabrica)
) {
    val usuarioActual by viewModelUsuario.usuarioActual.collectAsStateWithLifecycle()
    val estaCargandoPerfil by viewModelUsuario.estaCargandoPerfil.collectAsStateWithLifecycle()
    val estadoEdicionPerfil by viewModelUsuario.estadoEdicion.collectAsStateWithLifecycle()
    val listaEventos by viewModelEventos.listaEventos.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = RutaAplicacion.Login.ruta,
        modifier = modifier
    ) {
        composable(route = RutaAplicacion.Login.ruta) {
            LoginScreen(
                onLoginSuccess = { correoElectronico, _ ->
                    // El login solo captura el correo: si la base está vacía se crea
                    // un perfil mínimo para que "Mi perfil" tenga datos que leer.
                    viewModelUsuario.registrarIngreso(correoElectronico)
                    // Login e invitado comparten destino: el Home de eventos.
                    navController.navigate(RutaAplicacion.Eventos.ruta) {
                        popUpTo(RutaAplicacion.Login.ruta) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    // TODO: apuntar a la ruta Registro cuando exista; por ahora
                    // "más información" abre la pantalla institucional.
                    navController.navigate(RutaAplicacion.SobreNosotros.ruta)
                }
            )
        }

        composable(route = RutaAplicacion.Eventos.ruta) {
            EventosScreen(
                listaEventos = listaEventos,
                onAbrirDetalleEvento = { idEvento ->
                    navController.navigate(RutaAplicacion.DetalleEvento.crearRuta(idEvento))
                },
                onAbrirSobreNosotros = {
                    navController.navigate(RutaAplicacion.SobreNosotros.ruta)
                },
                onAbrirMiPerfil = {
                    navController.navigate(RutaAplicacion.MiPerfil.ruta)
                }
            )
        }

        composable(route = RutaAplicacion.SobreNosotros.ruta) {
            SobreNosotrosScreen(onVolver = { navController.popBackStack() })
        }

        composable(
            route = RutaAplicacion.DetalleEvento.ruta,
            arguments = listOf(
                navArgument(name = RutaAplicacion.DetalleEvento.ARGUMENTO_ID_EVENTO) {
                    type = NavType.LongType
                }
            )
        ) { entradaNavegacion ->
            val idEvento = entradaNavegacion.arguments
                ?.getLong(RutaAplicacion.DetalleEvento.ARGUMENTO_ID_EVENTO)
                ?: 0L
            DetalleEventoScreen(
                evento = viewModelEventos.obtenerEventoPorId(idEvento),
                onVolver = { navController.popBackStack() }
            )
        }

        composable(route = RutaAplicacion.MiPerfil.ruta) {
            MiPerfilScreen(
                usuario = usuarioActual,
                estaCargando = estaCargandoPerfil,
                onEditarPerfil = { navController.navigate(RutaAplicacion.EditarPerfil.ruta) },
                onVolver = { navController.popBackStack() }
            )
        }

        composable(route = RutaAplicacion.EditarPerfil.ruta) {
            EditarPerfilScreen(
                usuarioActual = usuarioActual,
                estaCargando = estaCargandoPerfil,
                estadoEdicion = estadoEdicionPerfil,
                onGuardarCambios = { nombreCompleto, correoElectronico, biografia, uriFotoPerfil ->
                    viewModelUsuario.guardarPerfil(
                        nombreCompleto = nombreCompleto,
                        correoElectronico = correoElectronico,
                        biografia = biografia,
                        uriFotoPerfil = uriFotoPerfil
                    )
                },
                onGuardadoExitoso = {
                    // Solo se regresa cuando Room confirmó el INSERT/UPDATE; el perfil
                    // se refresca solo gracias al Flow reactivo del ViewModel.
                    navController.popBackStack()
                },
                onLimpiarEstadoEdicion = viewModelUsuario::finalizarEdicion,
                onVolver = { navController.popBackStack() }
            )
        }
    }
}
