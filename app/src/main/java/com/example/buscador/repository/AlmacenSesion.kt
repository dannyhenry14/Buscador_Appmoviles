package com.example.buscador.repository

import android.content.Context
import androidx.core.content.edit

/**
 * Almacén local de la sesión: recuerda qué usuario tiene la sesión abierta
 * aunque la aplicación se cierre.
 *
 * Decisiones técnicas:
 * - Se guarda solo el correo del usuario, nunca la contraseña. Con el correo basta
 *   para buscar su perfil en Room al volver a abrir la app.
 * - Se usa SharedPreferences (archivo clave-valor del sistema) y no una tabla de
 *   Room, porque cambiar el esquema de la base borraría los usuarios registrados.
 * - Se toma el contexto de aplicación para no retener Activities.
 */
class AlmacenSesion(contexto: Context) {

    /** Archivo privado de la app donde se guarda la sesión; otras apps no pueden leerlo. */
    private val preferencias = contexto.applicationContext
        .getSharedPreferences(NOMBRE_ARCHIVO_SESION, Context.MODE_PRIVATE)

    /** Devuelve el correo de la sesión guardada, o null si no hay ninguna. */
    fun leerCorreoSesion(): String? =
        preferencias.getString(CLAVE_CORREO_SESION, null)

    /** Guarda el correo del usuario que acaba de iniciar sesión. */
    fun guardarCorreoSesion(correoElectronico: String) {
        preferencias.edit { putString(CLAVE_CORREO_SESION, correoElectronico) }
    }

    /** Borra la sesión guardada; se usa al cerrar sesión o entrar como invitado. */
    fun borrarSesion() {
        preferencias.edit { remove(CLAVE_CORREO_SESION) }
    }

    private companion object {
        const val NOMBRE_ARCHIVO_SESION = "sesion_buscador"
        const val CLAVE_CORREO_SESION = "correo_sesion"
    }
}