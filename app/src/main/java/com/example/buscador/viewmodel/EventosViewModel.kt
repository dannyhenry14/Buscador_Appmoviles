package com.example.buscador.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.buscador.BuscadorApplication
import com.example.buscador.repository.EventoRepository
import com.example.buscador.model.CategoriaEvento
import com.example.buscador.model.Evento
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.example.buscador.repository.ClimaRepository

/**
 * ViewModel de eventos (pantalla Home y detalle).
 *
 * Mantiene el listado como [StateFlow] para que la UI sea reactiva y no consulte
 * el repositorio en cada recomposición. La lectura del detalle es puntual porque
 * la navegación entrega el id como argumento.
 */
class EventosViewModel(
    private val repositorioEvento: EventoRepository,
    private val repositorioClima: ClimaRepository
) : ViewModel() {

    /** Listado de eventos publicado a la UI; inmutable desde fuera del ViewModel. */
    private val _listaEventos = MutableStateFlow<List<Evento>>(emptyList())
    val listaEventos: StateFlow<List<Evento>> = _listaEventos.asStateFlow()

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _clima = MutableStateFlow<com.example.buscador.model.ClimaActual?>(null)
    val clima: StateFlow<com.example.buscador.model.ClimaActual?> = _clima.asStateFlow()

    init {
        cargarTodosLosEventos()
        cargarClimaActual()
    }
    
    private fun cargarClimaActual() {
        viewModelScope.launch {
            _clima.value = repositorioClima.obtenerClimaSantiago()
        }
    }

    /**
     * Carga todos los eventos de la API inicializando la pantalla.
     */
    fun cargarTodosLosEventos() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                _listaEventos.value = repositorioEvento.obtenerTodosLosEventos()
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar los eventos: ${e.message}"
                _listaEventos.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * Devuelve el evento solicitado buscando en el listado cargado en memoria.
     * Al usar API remota, los eventos ya se descargaron al entrar a la pantalla principal.
     *
     * @return el evento o null cuando el id no existe.
     */
    fun obtenerEventoPorId(idEvento: Long): Evento? {
        return _listaEventos.value.firstOrNull { it.idEvento == idEvento }
    }

    /**
     * Aplica los filtros de búsqueda y publica el resultado a la UI.
     * Los filtros en null no se aplican (ver [EventoRepository.buscarEventos]).
     */
    fun buscarEventos(
        texto: String,
        categoria: CategoriaEvento?,
        fechaDesdeMillis: Long?,
        fechaHastaMillis: Long?
    ) {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                _listaEventos.value = repositorioEvento.buscarEventos(
                    texto = texto,
                    categoria = categoria,
                    fechaDesdeMillis = fechaDesdeMillis,
                    fechaHastaMillis = fechaHastaMillis
                )
            } catch (e: Exception) {
                _error.value = "Error al buscar eventos: ${e.message}"
                _listaEventos.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }

    companion object {
        /** Factory manual: inyecta el repositorio del contenedor de la Application. */
        val Fabrica: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val aplicacion = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as BuscadorApplication
                EventosViewModel(
                    repositorioEvento = aplicacion.contenedorAplicacion.repositorioEvento,
                    repositorioClima = aplicacion.contenedorAplicacion.repositorioClima
                )
            }
        }
    }
}