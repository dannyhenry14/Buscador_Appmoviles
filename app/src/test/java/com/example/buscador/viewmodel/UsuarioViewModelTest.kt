package com.example.buscador.viewmodel

import com.example.buscador.model.Usuario
import com.example.buscador.repository.UsuarioRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UsuarioViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val mockRepo = mockk<UsuarioRepository>(relaxed = true)

    @Test
    fun `iniciaConSesionAbierta es verdadero si hay sesion guardada`() {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "test@test.com"
        val viewModel = UsuarioViewModel(mockRepo)
        assertTrue(viewModel.iniciaConSesionAbierta)
    }

    @Test
    fun `validarCredenciales retorna true y guarda estado si repositorio autoriza`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns null
        coEvery { mockRepo.iniciarSesion("juan@test.com", "12345678") } returns Usuario(
            1L, "Juan", "juan@test.com", "12345678", ""
        )
        
        val viewModel = UsuarioViewModel(mockRepo)
        
        val result = viewModel.validarCredenciales("juan@test.com", "12345678")
        
        assertTrue(result)
        assertFalse(viewModel.credencialesIncorrectas.value)
    }

    @Test
    fun `validarCredenciales retorna false si repositorio rechaza`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns null
        coEvery { mockRepo.iniciarSesion("juan@test.com", "falsa") } returns null
        
        val viewModel = UsuarioViewModel(mockRepo)
        
        val result = viewModel.validarCredenciales("juan@test.com", "falsa")
        
        assertFalse(result)
        assertTrue(viewModel.credencialesIncorrectas.value)
    }

    @Test
    fun `cerrarSesion limpia identidad y borra sesion de preferencias`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "test@test.com"
        
        // Configuramos el mock para que observe el usuario del correo
        val flowMock = MutableStateFlow<Usuario?>(Usuario(1L, "Test", "test@test.com", "pass", ""))
        every { mockRepo.observarUsuarioPorCorreo("test@test.com") } returns flowMock
        
        val viewModel = UsuarioViewModel(mockRepo)
        advanceUntilIdle() // Dejar que el StateFlow inicial cargue
        
        viewModel.cerrarSesion()
        advanceUntilIdle() // Esperar propagacion de la corrutina
        
        io.mockk.verify { mockRepo.borrarSesionGuardada() }
        assertEquals(null, viewModel.usuarioActual.value)
    }

    @Test
    fun `guardarPerfil falla si nombre es invalido`() = runTest {
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.guardarPerfil("", "test@test.com", "bio", null)
        
        val estado = viewModel.estadoEdicion.value as EstadoEdicionPerfil.Error
        assertTrue(estado.mensaje.contains("obligatorio"))
    }

    @Test
    fun `guardarPerfil falla si correo es invalido`() = runTest {
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.guardarPerfil("Juan", "correo_malo", "bio", null)
        
        val estado = viewModel.estadoEdicion.value as EstadoEdicionPerfil.Error
        assertTrue(estado.mensaje.contains("válido"))
    }

    @Test
    fun `registrarUsuario falla si campos son invalidos`() = runTest {
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.actualizarNombreRegistro("")
        viewModel.actualizarCorreoRegistro("mal")
        viewModel.actualizarContrasenaRegistro("123") // Corta
        
        viewModel.registrarUsuario()
        
        val estado = viewModel.estadoRegistro.value
        assertFalse(estado.formularioEsValido)
        assertFalse(estado.estaRegistrando)
    }

    @Test
    fun `eliminarCuentaActual falla si no hay usuario en sesion`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns null
        val viewModel = UsuarioViewModel(mockRepo)
        advanceUntilIdle()
        
        viewModel.eliminarCuentaActual()
        advanceUntilIdle()
        
        val estado = viewModel.estadoEdicion.value
        assertEquals(EstadoEdicionPerfil.Inactivo, estado)
    }

    @Test
    fun `refrescarPerfilActual llama al repositorio si hay sesion`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "test@test.com"
        val flowMock = MutableStateFlow<Usuario?>(Usuario(1L, "Test", "test@test.com", "pass", ""))
        every { mockRepo.observarUsuarioPorCorreo("test@test.com") } returns flowMock
        
        coEvery { mockRepo.refrescarUsuarioPorId(1L) } returns Result.success(Usuario(1L, "Test", "test@test.com", "pass", ""))
        
        val viewModel = UsuarioViewModel(mockRepo)
        // Recolectar para que flatMapLatest se dispare
        val job = launch { viewModel.usuarioActual.collect { } }
        advanceUntilIdle()
        
        viewModel.refrescarPerfilActual()
        advanceUntilIdle()
        
        coVerify { mockRepo.refrescarUsuarioPorId(1L) }
        job.cancel()
    }

    @Test
    fun `guardarPerfil guarda usuario y actualiza estado a Guardado`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "old@test.com"
        val viewModel = UsuarioViewModel(mockRepo)
        
        coEvery { mockRepo.buscarUsuarioPorCorreo("old@test.com") } returns Usuario(1L, "Old", "old@test.com", "pass", "")
        coEvery { mockRepo.guardarUsuario(any()) } returns 1L

        viewModel.guardarPerfil("New Name", "old@test.com", "New bio", "uri")
        advanceUntilIdle()

        assertEquals(EstadoEdicionPerfil.Guardado, viewModel.estadoEdicion.value)
    }

    @Test
    fun `guardarPerfil lanza error si repositorio falla`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "old@test.com"
        val viewModel = UsuarioViewModel(mockRepo)
        
        coEvery { mockRepo.buscarUsuarioPorCorreo(any()) } returns null
        coEvery { mockRepo.guardarUsuario(any()) } throws RuntimeException("Error BD")

        viewModel.guardarPerfil("New Name", "new@test.com", "New bio", "uri")
        advanceUntilIdle()

        val estado = viewModel.estadoEdicion.value
        assertTrue(estado is EstadoEdicionPerfil.Error)
    }

    @Test
    fun `eliminarCuentaActual cierra sesion si repositorio retorna success`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "test@test.com"
        val flowMock = MutableStateFlow<Usuario?>(Usuario(1L, "Test", "test@test.com", "pass", ""))
        every { mockRepo.observarUsuarioPorCorreo("test@test.com") } returns flowMock
        
        coEvery { mockRepo.eliminarCuenta(1L) } returns Result.success(Unit)
        
        val viewModel = UsuarioViewModel(mockRepo)
        val job = launch { viewModel.usuarioActual.collect { } }
        advanceUntilIdle()
        
        viewModel.eliminarCuentaActual()
        advanceUntilIdle()
        
        io.mockk.verify { mockRepo.borrarSesionGuardada() }
        job.cancel()
    }

    @Test
    fun `eliminarCuentaActual muestra error si repositorio retorna failure`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "test@test.com"
        val flowMock = MutableStateFlow<Usuario?>(Usuario(1L, "Test", "test@test.com", "pass", ""))
        every { mockRepo.observarUsuarioPorCorreo("test@test.com") } returns flowMock
        
        coEvery { mockRepo.eliminarCuenta(1L) } returns Result.failure(Exception("Fallo"))
        
        val viewModel = UsuarioViewModel(mockRepo)
        val job = launch { viewModel.usuarioActual.collect { } }
        advanceUntilIdle()
        
        viewModel.eliminarCuentaActual()
        advanceUntilIdle()
        
        val estado = viewModel.estadoEdicion.value
        assertTrue(estado is EstadoEdicionPerfil.Error)
        job.cancel()
    }

    @Test
    fun `registrarUsuario registra exitosamente y actualiza estado`() = runTest {
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.actualizarNombreRegistro("Valido")
        viewModel.actualizarCorreoRegistro("valido@test.com")
        viewModel.actualizarContrasenaRegistro("12345678")
        
        coEvery { mockRepo.guardarUsuario(any()) } returns 1L
        
        viewModel.registrarUsuario()
        advanceUntilIdle()
        
        assertEquals("valido@test.com", viewModel.estadoRegistro.value.correoRegistrado)
        assertFalse(viewModel.estadoRegistro.value.estaRegistrando)
    }

    @Test
    fun `ingresarComoInvitado limpia sesion y guarda null`() = runTest {
        every { mockRepo.obtenerCorreoSesionGuardada() } returns "old@test.com"
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.ingresarComoInvitado()
        
        io.mockk.verify { mockRepo.borrarSesionGuardada() }
        val job = launch { viewModel.usuarioActual.collect { } }
        advanceUntilIdle()
        assertEquals(null, viewModel.usuarioActual.value)
        job.cancel()
    }

    @Test
    fun `finalizarEdicion cambia estado a inactivo`() = runTest {
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.finalizarEdicion()
        assertEquals(EstadoEdicionPerfil.Inactivo, viewModel.estadoEdicion.value)
    }

    @Test
    fun `limpiarEstadoRegistro resetea el estado`() = runTest {
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.actualizarNombreRegistro("Valido")
        viewModel.limpiarEstadoRegistro()
        assertEquals("", viewModel.estadoRegistro.value.nombre)
    }

    @Test
    fun `registrarUsuario falla por excepcion y genera error`() = runTest {
        val viewModel = UsuarioViewModel(mockRepo)
        viewModel.actualizarNombreRegistro("Valido")
        viewModel.actualizarCorreoRegistro("valido@test.com")
        viewModel.actualizarContrasenaRegistro("12345678")
        
        coEvery { mockRepo.guardarUsuario(any()) } throws RuntimeException("Error")
        
        viewModel.registrarUsuario()
        advanceUntilIdle()
        
        assertTrue(viewModel.estadoRegistro.value.mensajeError != null)
    }
}
