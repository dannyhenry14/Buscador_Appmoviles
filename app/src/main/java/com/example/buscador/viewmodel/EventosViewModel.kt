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
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.combine
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

    /** Listado de eventos cargado desde la API. */
    private val _todosLosEventos = MutableStateFlow<List<Evento>>(emptyList())
    
    /** Estados de filtros. */
    private val _textoBusqueda = MutableStateFlow("")
    private val _filtrosCategoria = MutableStateFlow<CategoriaEvento?>(null)
    private val _filtrosFechaDesde = MutableStateFlow<Long?>(null)
    private val _filtrosFechaHasta = MutableStateFlow<Long?>(null)
    
    /** Combina los eventos remotos con los filtros de búsqueda. */
    val listaEventos: StateFlow<List<Evento>> = combine(
        _todosLosEventos,
        _textoBusqueda,
        _filtrosCategoria,
        _filtrosFechaDesde,
        _filtrosFechaHasta
    ) { eventos, texto, categoria, fechaDesde, fechaHasta ->
        val textoLimpio = texto.trim()
        eventos.filter { evento ->
            val coincideTexto = textoLimpio.isEmpty() ||
                evento.titulo.contains(textoLimpio, ignoreCase = true) ||
                evento.descripcion.contains(textoLimpio, ignoreCase = true) ||
                evento.ubicacion.contains(textoLimpio, ignoreCase = true)
            
            val coincideCategoria = categoria == null || evento.categoria == categoria
            val coincideDesde = fechaDesde == null || evento.fechaMillis >= fechaDesde
            val coincideHasta = fechaHasta == null || evento.fechaMillis <= fechaHasta
            
            coincideTexto && coincideCategoria && coincideDesde && coincideHasta
        }
    }.stateIn(
        scope = viewModelScope,
        started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _cargando = MutableStateFlow(false)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _clima = MutableStateFlow<com.example.buscador.model.ClimaActual?>(null)
    val clima: StateFlow<com.example.buscador.model.ClimaActual?> = _clima.asStateFlow()

    private var eventosCargados = false
    
    init {
        cargarDatosIniciales()
    }
    
    private fun cargarDatosIniciales() {
        if (!eventosCargados) {
            cargarTodosLosEventos()
        }
        cargarClimaActual()
    }
    
    private fun cargarClimaActual() {
        viewModelScope.launch {
            _clima.value = repositorioClima.obtenerClimaSantiago()
        }
    }

    /**
     * Carga todos los eventos de la API. Se ejecuta una sola vez.
     */
    fun cargarTodosLosEventos() {
        viewModelScope.launch {
            _cargando.value = true
            _error.value = null
            try {
                _todosLosEventos.value = repositorioEvento.obtenerTodosLosEventos()
                eventosCargados = true
            } catch (e: Exception) {
                _error.value = "No se pudieron cargar los eventos: ${e.message}"
                _todosLosEventos.value = emptyList()
            } finally {
                _cargando.value = false
            }
        }
    }

    /**
     * Devuelve el evento solicitado buscando en el listado cargado en memoria.
     * @return el evento o null cuando el id no existe.
     */
    fun obtenerEventoPorId(idEvento: Long): Evento? {
        return _todosLosEventos.value.firstOrNull { it.idEvento == idEvento }
    }

    /**
     * Aplica los filtros de búsqueda y publica el resultado a la UI.
     */
    fun buscarEventos(
        texto: String,
        categoria: CategoriaEvento?,
        fechaDesdeMillis: Long?,
        fechaHastaMillis: Long?
    ) {
        _textoBusqueda.value = texto
        _filtrosCategoria.value = categoria
        _filtrosFechaDesde.value = fechaDesdeMillis
        _filtrosFechaHasta.value = fechaHastaMillis
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