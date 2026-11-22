package com.example.buscador.repository

import com.example.buscador.model.Usuario
import com.example.buscador.model.UsuarioEntity
import com.example.buscador.remote.CenamadApi
import com.example.buscador.remote.UsuarioDto
import com.example.buscador.remote.UsuarioRequestDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class UsuarioRepositoryImplTest {

    private val mockApi = mockk<CenamadApi>(relaxed = true)
    private val mockDao = mockk<UsuarioDao>(relaxed = true)
    private val mockAlmacen = mockk<AlmacenSesion>(relaxed = true)
    
    private val repositorio = UsuarioRepositoryImpl(mockApi, mockDao, mockAlmacen)

    @Test
    fun `iniciarSesion retorna usuario si credenciales coinciden en remotos y guarda sesion`() = runTest {
        val dtos = listOf(
            UsuarioDto(1, "Juan", "juan@test.com", "12345678")
        )
        coEvery { mockApi.obtenerUsuarios() } returns dtos
        coEvery { mockDao.buscarUsuarioPorCorreo("juan@test.com") } returns null
        coEvery { mockDao.insertarUsuario(any()) } returns 1L

        val resultado = repositorio.iniciarSesion("juan@test.com", "12345678")

        assertEquals("Juan", resultado?.nombreCompleto)
        verify { mockAlmacen.guardarCorreoSesion("juan@test.com") }
    }

    @Test
    fun `iniciarSesion retorna null si credenciales no coinciden en remotos`() = runTest {
        val dtos = listOf(
            UsuarioDto(1, "Juan", "juan@test.com", "12345678")
        )
        coEvery { mockApi.obtenerUsuarios() } returns dtos

        val resultado = repositorio.iniciarSesion("juan@test.com", "incorrecta")

        assertNull(resultado)
    }

    @Test
    fun `guardarUsuario inserta nuevo si id es SIN_ASIGNAR`() = runTest {
        val nuevoUsuario = Usuario(
            idUsuario = Usuario.ID_SIN_ASIGNAR,
            nombreCompleto = "Nuevo",
            correoElectronico = "nuevo@test.com",
            contrasena = "pass",
            biografia = ""
        )
        val dtoRespuesta = UsuarioDto(2, "Nuevo", "nuevo@test.com", "pass")
        coEvery { mockApi.crearUsuario(any()) } returns dtoRespuesta
        coEvery { mockDao.insertarUsuario(any()) } returns 2L

        val idGenerado = repositorio.guardarUsuario(nuevoUsuario)

        assertEquals(2L, idGenerado)
        verify { mockAlmacen.guardarCorreoSesion("nuevo@test.com") }
    }

    @Test
    fun `refrescarUsuarioPorId actualiza y retorna usuario remoto`() = runTest {
        val dto = UsuarioDto(1, "Juan Editado", "juan@test.com", "pass")
        coEvery { mockApi.obtenerUsuarioPorId(1L) } returns dto
        
        val result = repositorio.refrescarUsuarioPorId(1L)
        
        assertTrue(result.isSuccess)
        assertEquals("Juan Editado", result.getOrNull()?.nombreCompleto)
    }

    @Test
    fun `eliminarCuenta borra usuario y sesion si API responde exitoso`() = runTest {
        coEvery { mockApi.eliminarUsuario(1L) } returns Response.success(Unit)
        every { mockAlmacen.leerCorreoSesion() } returns "test@test.com"
        coEvery { mockDao.buscarUsuarioPorCorreo("test@test.com") } returns UsuarioEntity(1L, "Test", "test@test.com", "pass", "", null)

        val result = repositorio.eliminarCuenta(1L)

        assertTrue(result.isSuccess)
        verify { mockAlmacen.borrarSesion() }
    }

    @Test
    fun `guardarUsuario actualiza usuario existente si id no es SIN_ASIGNAR`() = runTest {
        val usuarioEditado = Usuario(
            idUsuario = 1L,
            nombreCompleto = "Editado",
            correoElectronico = "test@test.com",
            contrasena = "pass",
            biografia = "Nueva bio"
        )
        val dtoRespuesta = UsuarioDto(1, "Editado", "test@test.com", "pass", "Nueva bio")
        coEvery { mockApi.actualizarUsuario(1, any()) } returns dtoRespuesta

        val idGenerado = repositorio.guardarUsuario(usuarioEditado)

        assertEquals(1L, idGenerado)
        coVerify { mockDao.actualizarUsuario(any()) }
    }

    @Test
    fun `iniciarSesion retorna fallback local si API falla`() = runTest {
        coEvery { mockApi.obtenerUsuarios() } throws RuntimeException("Sin red")
        val entidadLocal = UsuarioEntity(1L, "Test", "test@test.com", "pass", "", null)
        coEvery { mockDao.buscarUsuarioPorCorreo("test@test.com") } returns entidadLocal

        val resultado = repositorio.iniciarSesion("test@test.com", "pass")

        assertEquals("Test", resultado?.nombreCompleto)
        verify { mockAlmacen.guardarCorreoSesion("test@test.com") }
    }

    @Test
    fun `refrescarUsuarioPorId retorna failure si API lanza excepcion`() = runTest {
        coEvery { mockApi.obtenerUsuarioPorId(1L) } throws RuntimeException("Error")
        
        val result = repositorio.refrescarUsuarioPorId(1L)
        
        assertTrue(result.isFailure)
    }

    @Test
    fun `eliminarCuenta retorna failure si API falla`() = runTest {
        coEvery { mockApi.eliminarUsuario(1L) } throws RuntimeException("Timeout")

        val result = repositorio.eliminarCuenta(1L)

        assertTrue(result.isFailure)
    }

    @Test
    fun `iniciarSesion lanza excepcion si API falla y fallback local no coincide`() = runTest {
        coEvery { mockApi.obtenerUsuarios() } throws RuntimeException("Sin red")
        val entidadLocal = UsuarioEntity(1L, "Test", "test@test.com", "pass", "", null)
        coEvery { mockDao.buscarUsuarioPorCorreo("test@test.com") } returns entidadLocal

        var lanzoExcepcion = false
        try {
            repositorio.iniciarSesion("test@test.com", "malapass")
        } catch (e: Exception) {
            lanzoExcepcion = true
        }
        assertTrue(lanzoExcepcion)
    }

    @Test
    fun `actualizarFotoPerfil delega a daoUsuario`() = runTest {
        repositorio.actualizarFotoPerfil(1L, "nueva_uri")
        coVerify { mockDao.actualizarUriFotoPerfil(1L, "nueva_uri") }
    }

    @Test
    fun `eliminarUsuario delega a daoUsuario`() = runTest {
        val usuario = Usuario(1L, "Test", "test@test.com", "pass", "")
        repositorio.eliminarUsuario(usuario)
        coVerify { mockDao.eliminarUsuario(any()) }
    }
}