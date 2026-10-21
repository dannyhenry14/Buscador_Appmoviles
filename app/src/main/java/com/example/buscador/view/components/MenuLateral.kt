package com.example.buscador.view.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.buscador.navigation.RutaAplicacion
import com.example.buscador.ui.theme.DarkGreen
import com.example.buscador.ui.theme.GrisBordeCampo
import com.example.buscador.ui.theme.White
import com.example.buscador.ui.theme.YellowDuoc

/**
 * Entrada del menú lateral: asocia una sección de nivel superior con su
 * presentación (etiqueta e ícono). Se declara como lista constante para que
 * añadir una sección solo requiera un elemento nuevo aquí y su ruta.
 */
private data class ItemMenuLateral(
    val rutaDestino: RutaAplicacion,
    val etiqueta: String,
    val icono: ImageVector
)

/** Secciones navegables desde el menú, en el orden en que se muestran. */
private val itemsMenuLateral = listOf(
    ItemMenuLateral(
        rutaDestino = RutaAplicacion.Eventos,
        etiqueta = "Inicio",
        icono = Icons.Filled.Home
    ),
    ItemMenuLateral(
        rutaDestino = RutaAplicacion.Mapa,
        etiqueta = "Mapa",
        icono = Icons.Filled.Place
    ),
    ItemMenuLateral(
        rutaDestino = RutaAplicacion.SobreNosotros,
        etiqueta = "Sobre nosotros",
        icono = Icons.Filled.Info
    ),
    ItemMenuLateral(
        rutaDestino = RutaAplicacion.MiPerfil,
        etiqueta = "Perfil",
        icono = Icons.Filled.Person
    )
)

/**
 * Contenido del menú lateral de navegación (ModalNavigationDrawer).
 *
 * Diseñado como la referencia visual del proyecto: panel verde institucional
 * con encabezado de marca, ítems de sección con indicador de selección y la
 * marca "Duoc UC | CITT" una única vez, en el pie del panel.
 *
 * Es stateless respecto a la navegación: recibe la ruta actual para pintar
 * el ítem seleccionado y delega el cierre del drawer y la navegación al
 * callback [onNavegarASeccion], que resuelve el NavHost.
 *
 * @param rutaSeleccionada Ruta de la sección visible; null si la ruta actual
 *   no es una sección de nivel superior (ningún ítem queda marcado).
 * @param onNavegarASeccion Invocado con la ruta elegida por el usuario.
 */
@Composable
fun MenuLateral(
    rutaSeleccionada: String?,
    onNavegarASeccion: (RutaAplicacion) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = DarkGreen
    ) {
        // Encabezado de marca: logo sol + wordmark + sede institucional.
        Column(
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 28.dp, bottom = 8.dp)
        ) {
            LogoSolCenamad(tamano = 30.dp, colorPrimario = White, colorSecundario = White)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "CENAMAD",
                color = White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Universidad de San Joaquín",
                color = White.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        itemsMenuLateral.forEach { item ->
            NavigationDrawerItem(
                label = {
                    Text(text = item.etiqueta, fontWeight = FontWeight.SemiBold)
                },
                icon = {
                    Icon(
                        imageVector = item.icono,
                        contentDescription = null
                    )
                },
                selected = rutaSeleccionada == item.rutaDestino.ruta,
                onClick = { onNavegarASeccion(item.rutaDestino) },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = White.copy(alpha = 0.16f),
                    unselectedContainerColor = Color.Transparent,
                    selectedIconColor = YellowDuoc,
                    unselectedIconColor = White.copy(alpha = 0.75f),
                    selectedTextColor = White,
                    unselectedTextColor = White.copy(alpha = 0.9f)
                ),
                modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
        }

        // La marca corporativa se menciona una sola vez, anclada al pie del panel.
        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.Start
        ) {
            MarcaInstitucional(
                tamanoTexto = 14.sp,
                colorDuoc = YellowDuoc,
                colorUC = White,
                colorSeparador = GrisBordeCampo,
                colorCITT = White
            )
        }
    }
}
