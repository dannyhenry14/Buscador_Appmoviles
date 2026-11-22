package com.example.buscador.viewmodel

import com.example.buscador.model.CategoriaEvento
import com.example.buscador.model.ClimaActual
import com.example.buscador.model.Evento
import com.example.buscador.repository.ClimaRepository
import com.example.buscador.repository.EventoRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EventosViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockEventoRepo = mockk<EventoRepository>()
    private val mockClimaRepo = mockk<ClimaRepository>()

    @Test
    fun `init carga eventos y clima correctamente`() = runTest {
        val eventos = listOf(
            Evento(1L, "Titulo", "Desc", "Hoy", 1000L, "Sede", CategoriaEvento.SEMINARIO)
        )
        coEvery { mockEventoRepo.obtenerTodosLosEventos() } returns eventos
        coEvery { mockClimaRepo.obtenerClimaSantiago() } returns ClimaActual(20.0, 10.0, "Sol")

        val viewModel = EventosViewModel(mockEventoRepo, mockClimaRepo)
        
        // Se debe recolectar el StateFlow para que el combine() se ejecute en el test
        val job = launch { viewModel.listaEventos.collect { } }
        advanceUntilIdle() // Espera a que terminen las corrutinas del init

        assertEquals(20.0, viewModel.clima.value?.temperatura)
        assertEquals(1, viewModel.listaEventos.value.size)
        assertEquals("Titulo", viewModel.listaEventos.value[0].titulo)
        
        job.cancel()
    }

    @Test
    fun `buscarEventos filtra lista en memoria`() = runTest {
        val eventos = listOf(
            Evento(1L, "Titulo 1", "Desc", "Hoy", 1000L, "Sede", CategoriaEvento.SEMINARIO),
            Evento(2L, "Otro", "Desc", "Hoy", 1000L, "Sede", CategoriaEvento.TALLER)
        )
        coEvery { mockEventoRepo.obtenerTodosLosEventos() } returns eventos
        coEvery { mockClimaRepo.obtenerClimaSantiago() } returns null

        val viewModel = EventosViewModel(mockEventoRepo, mockClimaRepo)
        val job = launch { viewModel.listaEventos.collect { } }
        advanceUntilIdle()

        viewModel.buscarEventos(
            texto = "Otro",
            categoria = null,
            fechaDesdeMillis = null,
            fechaHastaMillis = null
        )
        
        advanceUntilIdle()

        assertEquals(1, viewModel.listaEventos.value.size)
        assertEquals("Otro", viewModel.listaEventos.value[0].titulo)
        
        job.cancel()
    }

    @Test
    fun `obtenerEventoPorId retorna evento si existe`() = runTest {
        val eventos = listOf(
            Evento(1L, "Titulo 1", "Desc", "Hoy", 1000L, "Sede", CategoriaEvento.SEMINARIO)
        )
        coEvery { mockEventoRepo.obtenerTodosLosEventos() } returns eventos
        coEvery { mockClimaRepo.obtenerClimaSantiago() } returns null

        val viewModel = EventosViewModel(mockEventoRepo, mockClimaRepo)
        val job = launch { viewModel.listaEventos.collect { } }
        advanceUntilIdle()

        val evento = viewModel.obtenerEventoPorId(1L)
        assertNotNull(evento)
        assertEquals("Titulo 1", evento?.titulo)
        
        job.cancel()
    }
}
