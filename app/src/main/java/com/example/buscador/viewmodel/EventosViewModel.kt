package com.example.buscador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buscador.BuscadorApplication
import com.example.buscador.data.repository.EventoRepository
import com.example.buscador.model.Evento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel de eventos (pantalla Home y detalle).
 *
 * Mantiene el listado como [StateFlow] para que la UI sea reactiva y no consulte
 * el repositorio en cada recomposición. La lectura del detalle es puntual porque
 * la navegación entrega el id como argumento.
 */
class EventosViewModel(
    private val repositorioEvento: EventoRepository
) : ViewModel() {

    /** Listado de eventos publicado a la UI; inmutable desde fuera del ViewModel. */
    private val _listaEventos = MutableStateFlow(repositorioEvento.obtenerTodosLosEventos())
    val listaEventos: StateFlow<List<Evento>> = _listaEventos.asStateFlow()

    /**
     * Devuelve el evento solicitado por la ruta de navegación.
     * @return el evento o null cuando el id no existe (ruta manipulada o dato eliminado).
     */
    fun obtenerEventoPorId(idEvento: Long): Evento? =
        repositorioEvento.obtenerEventoPorId(idEvento)

    companion object {
        /** Factory manual: inyecta el repositorio del contenedor de la Application. */
        val Fabrica: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                    as BuscadorApplication
                EventosViewModel(aplicacion.contenedorAplicacion.repositorioEvento)
            }
        }
    }
}
