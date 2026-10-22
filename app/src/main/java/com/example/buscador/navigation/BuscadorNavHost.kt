package com.example.buscador.navigation

import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.buscador.view.LoginScreen
import com.example.buscador.view.components.MenuLateral
import com.example.buscador.view.eventos.DetalleEventoScreen
import com.example.buscador.view.eventos.EventosScreen
import com.example.buscador.view.mapa.MapScreen
import com.example.buscador.view.nosotros.SobreNosotrosScreen
import com.example.buscador.view.perfil.EditarPerfilScreen
import com.example.buscador.view.perfil.MiPerfilScreen
import com.example.buscador.viewmodel.EventosViewModel
import com.example.buscador.viewmodel.UsuarioViewModel
import kotlinx.coroutines.launch

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
 * - Las secciones de nivel superior viven dentro de un `ModalNavigationDrawer`
 *   compartido. El gesto de borde y el botón hamburguesa solo están habilitados
 *   en esas rutas: Login, DetalleEvento y EditarPerfil conservan su propio flujo
 *   con "Volver" sin menú. Al navegar entre secciones se usa `saveState` +
 *   `restoreState` + `launchSingleTop` para que cada sección conserve su estado
 *   y no se dupliquen entradas en el back stack.
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
    val credencialesIncorrectas by viewModelUsuario.credencialesIncorrectas
        .collectAsStateWithLifecycle()
    val listaEventos by viewModelEventos.listaEventos.collectAsStateWithLifecycle()

    // Estado del menú lateral. Vive en el NavHost (no en las pantallas) porque
    // el drawer envuelve todo el grafo y es él quien abre/cierra el panel.
    val estadoMenuLateral = rememberDrawerState(initialValue = DrawerValue.Closed)
    val alcanceCorrutinas = rememberCoroutineScope()

    // Ruta actual para marcar la sección activa en el menú y habilitar el
    // gesto de borde solo donde corresponde.
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val menuLateralHabilitado = RutaAplicacion.seccionesConMenu
        .any { seccion -> seccion.ruta == rutaActual }

    // Abre el menú desde el botón hamburguesa de cualquier sección principal.
    val abrirMenuLateral: () -> Unit = {
        alcanceCorrutinas.launch { estadoMenuLateral.open() }
    }

    ModalNavigationDrawer(
        drawerState = estadoMenuLateral,
        gesturesEnabled = menuLateralHabilitado,
        drawerContent = {
            MenuLateral(
                rutaSeleccionada = rutaActual,
                onNavegarASeccion = { rutaDestino ->
                    // Se cierra el panel y se navega con saveState/restoreState
                    // para conservar el estado de cada sección al volver.
                    alcanceCorrutinas.launch {
                        estadoMenuLateral.close()
                        navController.navigate(rutaDestino.ruta) {
                            popUpTo(RutaAplicacion.Eventos.ruta) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = RutaAplicacion.Login.ruta,
            modifier = modifier
        ) {
            composable(
                route = RutaAplicacion.Login.ruta,
                arguments = listOf(
                    navArgument(name = RutaAplicacion.Login.ARGUMENTO_AVISO) {
                        type = NavType.StringType
                        defaultValue = ""
                    }
                )
            ) { entradaLogin ->
                LoginScreen(
                    // Sin aviso en la ruta, la pantalla usa su banner por defecto;
                    // el argumento solo aporta mensajes contextuales.
                    avisoRegistro = entradaLogin.arguments
                        ?.getString(RutaAplicacion.Login.ARGUMENTO_AVISO)
                        ?.takeIf { aviso -> aviso.isNotBlank() },
                    credencialesIncorrectas = credencialesIncorrectas,
                    onLoginSuccess = { correoElectronico, contrasena ->
                        // Autenticación simulada: solo con el usuario de prueba se
                        // abre la sesión. Con credenciales incorrectas no se navega;
                        // el ViewModel enciende el error que marcan los campos.
                        if (viewModelUsuario.validarCredenciales(
                                correoElectronico = correoElectronico,
                                contrasena = contrasena
                            )
                        ) {
                            // Si la base está vacía se crea un perfil mínimo para
                            // que "Mi perfil" tenga datos que leer.
                            viewModelUsuario.registrarIngreso(correoElectronico)
                            navController.navigate(RutaAplicacion.Eventos.ruta) {
                                popUpTo(RutaAplicacion.Login.ruta) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    },
                    onIngresarComoInvitado = {
                        // El usuario activo pasa a null en memoria antes de navegar:
                        // "Mi perfil" muestra su estado vacío sin arrastrar la fila
                        // persistida en Room.
                        viewModelUsuario.ingresarComoInvitado()
                        navController.navigate(RutaAplicacion.Eventos.ruta) {
                            popUpTo(RutaAplicacion.Login.ruta) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onLimpiarErrorCredenciales = viewModelUsuario::limpiarErrorCredenciales,
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
                    onAbrirMenu = abrirMenuLateral
                )
            }

            composable(route = RutaAplicacion.Mapa.ruta) {
                MapScreen(onAbrirMenu = abrirMenuLateral)
            }

            composable(route = RutaAplicacion.SobreNosotros.ruta) {
                SobreNosotrosScreen(onAbrirMenu = abrirMenuLateral)
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
                    onRegistrarUsuario = {
                        // El invitado no crea perfil directamente: el registro comienza
                        // en el login, al que se vuelve limpiando el back stack y con el
                        // aviso que explica cómo crear la cuenta.
                        navController.navigate(
                            RutaAplicacion.Login.crearRutaConAviso(
                                aviso = RutaAplicacion.Login.AVISO_REGISTRO_INVITADO
                            )
                        ) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onCerrarSesion = {
                        // Confirmado el diálogo: la sesión termina en el ViewModel
                        // y el grafo vuelve a nacer en el login. popUpTo sobre el
                        // propio grafo limpia todo el back stack, de modo que
                        // "Atrás" no regrese a las secciones de la sesión cerrada.
                        viewModelUsuario.cerrarSesion()
                        navController.navigate(RutaAplicacion.Login.ruta) {
                            popUpTo(navController.graph.id) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onAbrirMenu = abrirMenuLateral
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
}
