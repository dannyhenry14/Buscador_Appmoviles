package com.example.buscador.repository

import com.example.buscador.remote.CenamadApi
import com.example.buscador.remote.EventoDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class EventoRepositoryRemotoTest {

    private val mockApi = mockk<CenamadApi>()
    private val repositorio = EventoRepositoryRemoto(mockApi)

    @Test
    fun `obtenerTodosLosEventos retorna lista mapeada si API responde bien`() = runTest {
        val dtos = listOf(
            EventoDto(
                id = 1,
                titulo = "Test",
                descripcion = "Desc",
                fecha = "Hoy",
                ubicacion = "Sede",
                categoria = "SEMINARIO",
                imagen = null
            )
        )
        coEvery { mockApi.obtenerEventos() } returns dtos

        val resultado = repositorio.obtenerTodosLosEventos()

        assertEquals(1, resultado.size)
        assertEquals(1L, resultado[0].idEvento)
        assertEquals("Test", resultado[0].titulo)
    }

    @Test
    fun `obtenerTodosLosEventos retorna lista vacia si API lanza excepcion`() = runTest {
        coEvery { mockApi.obtenerEventos() } throws RuntimeException("Error red")

        val resultado = repositorio.obtenerTodosLosEventos()

        assertTrue(resultado.isEmpty())
    }

    @Test
    fun `obtenerEventoPorId retorna evento mapeado si API responde bien`() = runTest {
        val dto = EventoDto(
            id = 2,
            titulo = "Evento 2",
            descripcion = "Desc 2",
            fecha = "Manana",
            ubicacion = "Lab",
            categoria = "TALLER",
            imagen = null
        )
        coEvery { mockApi.obtenerEventoPorId(2) } returns dto

        val resultado = repositorio.obtenerEventoPorId(2L)

        assertEquals(2L, resultado?.idEvento)
        assertEquals("Evento 2", resultado?.titulo)
    }

    @Test
    fun `obtenerEventoPorId retorna null si API lanza excepcion`() = runTest {
        coEvery { mockApi.obtenerEventoPorId(2) } throws RuntimeException("Error red")

        val resultado = repositorio.obtenerEventoPorId(2L)

        assertNull(resultado)
    }
}
