package com.example.buscador.model

/**
 * Modelo de dominio del usuario de CENAMAD.
 *
 * Es el objeto que circula entre los ViewModel y las pantallas; no conoce Room
 * ni ninguna otra fuente de datos. La conversión hacia/desde la base de datos
 * vive en data/local/entity/UsuarioEntity.kt (separación de responsabilidades MVVM).
 *
 * @property idUsuario Clave primaria generada por Room. Vale [ID_SIN_ASIGNAR] mientras
 * el usuario aún no ha sido insertado en la base de datos.
 * @property nombreCompleto Nombre y apellidos que se muestran en el perfil.
 * @property correoElectronico Correo institucional del usuario (único en la base de datos).
 * @property biografia Texto libre que describe al usuario dentro de la app.
 * @property uriFotoPerfil URI persistida de la foto de perfil (content:// del selector de fotos).
 */
data class Usuario(
    val idUsuario: Long = ID_SIN_ASIGNAR,
    val nombreCompleto: String,
    val correoElectronico: String,
    val biografia: String,
    val uriFotoPerfil: String? = null
) {
    companion object {
        /** Valor centinela para distinguir un usuario nuevo de uno ya persistido. */
        const val ID_SIN_ASIGNAR = 0L
    }
}
