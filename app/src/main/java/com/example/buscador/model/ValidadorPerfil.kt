package com.example.buscador.model

/**
 * Reglas de validación del perfil compartidas por la UI y el ViewModel.
 *
 * Se centralizan aquí para que el formulario (que habilita el botón "Guardar")
 * y el ViewModel (última defensa antes de escribir en Room) apliquen
 * exactamente el mismo criterio.
 */
object ValidadorPerfil {

    /**
     * Correo electrónico práctico: usuario, arroba, dominio y extensión.
     * No pretende cubrir la RFC 5322 completa, solo evitar persistir datos inservibles.
     */
    private val PATRON_CORREO_ELECTRONICO =
        Regex(pattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    /** El nombre es obligatorio: es el dato que identifica al usuario en el perfil. */
    fun esNombreValido(nombreCompleto: String): Boolean = nombreCompleto.isNotBlank()

    /** Valida el correo electrónico antes de insertarlo o actualizarlo en Room. */
    fun esCorreoValido(correoElectronico: String): Boolean =
        PATRON_CORREO_ELECTRONICO.matches(input = correoElectronico.trim())
}
