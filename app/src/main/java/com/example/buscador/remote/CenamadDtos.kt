package com.example.buscador.remote

import com.example.buscador.model.CategoriaEvento
import com.example.buscador.model.Evento
import com.example.buscador.model.Usuario
import com.google.gson.annotations.SerializedName

/** DTO para la lectura de eventos desde Xano. */
data class EventoDto(
    @SerializedName("id") val id: Int,
    @SerializedName("created_at") val createdAt: Long? = null,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("descripcion") val descripcion: String,
    @SerializedName("fecha") val fecha: String,
    @SerializedName("ubicacion") val ubicacion: String,
    @SerializedName("categoria") val categoria: String,
    @SerializedName("imagen") val imagen: String? = null
)

/** DTO para la lectura de usuarios desde Xano. */
data class UsuarioDto(
    @SerializedName("id") val id: Int,
    @SerializedName("nombre_completo") val nombreCompleto: String,
    @SerializedName("correo_electronico") val correoElectronico: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("biografia") val biografia: String? = "",
    @SerializedName("uri_foto_perfil") val uriFotoPerfil: String? = ""
)

/** DTO para creación y actualización (POST/PATCH) de usuarios en Xano (sin id). */
data class UsuarioRequestDto(
    @SerializedName("nombre_completo") val nombreCompleto: String,
    @SerializedName("correo_electronico") val correoElectronico: String,
    @SerializedName("contrasena") val contrasena: String,
    @SerializedName("biografia") val biografia: String?,
    @SerializedName("uri_foto_perfil") val uriFotoPerfil: String?
)

// --- Funciones Mappers ---

/** Convierte un EventoDto a un Evento del dominio de la app. */
fun EventoDto.aModeloDominio(): Evento {
    // Mapeo seguro de la categoría devuelta por el API.
    val categoriaEnum = try {
        CategoriaEvento.valueOf(categoria.uppercase())
    } catch (e: Exception) {
        CategoriaEvento.SEMINARIO // Fallback por defecto si la categoría no coincide
    }

    return Evento(
        idEvento = id.toLong(),
        titulo = titulo,
        descripcion = descripcion,
        fecha = fecha,
        fechaMillis = createdAt ?: 0L, // Se asume createdAt como fecha de referencia si está
        ubicacion = ubicacion,
        categoria = categoriaEnum,
        imagenResId = 0, // Las imágenes remotas no usan ID de recurso local
        imagenUrl = imagen
    )
}

/** Convierte un UsuarioDto a un Usuario del dominio. */
fun UsuarioDto.aModeloDominio(): Usuario {
    return Usuario(
        idUsuario = id.toLong(),
        nombreCompleto = nombreCompleto,
        correoElectronico = correoElectronico,
        contrasena = contrasena,
        biografia = biografia ?: "",
        uriFotoPerfil = uriFotoPerfil
    )
}

/** Convierte un modelo de dominio Usuario a UsuarioRequestDto para enviar a Xano. */
fun Usuario.aRequestDto(): UsuarioRequestDto {
    return UsuarioRequestDto(
        nombreCompleto = nombreCompleto,
        correoElectronico = correoElectronico,
        contrasena = contrasena,
        biografia = biografia,
        uriFotoPerfil = uriFotoPerfil
    )
}
