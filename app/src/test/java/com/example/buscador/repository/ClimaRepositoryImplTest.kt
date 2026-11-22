package com.example.buscador.repository

import com.example.buscador.remote.ClimaApi
import com.example.buscador.remote.ClimaActualDto
import com.example.buscador.remote.ClimaResponseDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ClimaRepositoryImplTest {

    private val mockApi = mockk<ClimaApi>()
    private val repositorio = ClimaRepositoryImpl(mockApi)

    @Test
    fun `obtenerClimaSantiago retorna clima actual mappeado correctamente`() = runTest {
        val respuestaApi = ClimaResponseDto(
            climaActual = ClimaActualDto(
                temperature = 25.0,
                windspeed = 10.5,
                weathercode = 0
            )
        )
        coEvery { mockApi.obtenerClima() } returns respuestaApi

        val resultado = repositorio.obtenerClimaSantiago()

        assertEquals(25.0, resultado?.temperatura)
        assertEquals(10.5, resultado?.viento)
        assertEquals("Despejado", resultado?.condicion)
    }

    @Test
    fun `obtenerClimaSantiago retorna null si falla red`() = runTest {
        coEvery { mockApi.obtenerClima() } throws RuntimeException("Error")

        val resultado = repositorio.obtenerClimaSantiago()

        assertNull(resultado)
    }
}
