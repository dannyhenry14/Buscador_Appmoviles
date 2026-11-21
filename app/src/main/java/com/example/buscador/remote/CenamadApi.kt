package com.example.buscador.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Interfaz de Endpoints para la API de Xano.
 */
interface CenamadApi {

    @GET("eventos")
    suspend fun obtenerEventos(): List<EventoDto>

    @GET("eventos/{eventos_id}")
    suspend fun obtenerEventoPorId(@Path("eventos_id") id: Int): EventoDto

    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioDto>

    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: UsuarioRequestDto): UsuarioDto

    @PATCH("usuarios/{usuarios_id}")
    suspend fun actualizarUsuario(
        @Path("usuarios_id") id: Int, 
        @Body usuario: UsuarioRequestDto
    ): UsuarioDto

}
