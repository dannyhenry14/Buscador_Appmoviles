package com.example.buscador.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidadorPerfilTest {

    @Test
    fun `esNombreValido retorna true si no esta vacio ni es solo espacios`() {
        assertTrue(ValidadorPerfil.esNombreValido("Juan Perez"))
        assertTrue(ValidadorPerfil.esNombreValido("  Maria  ")) // contains characters
        assertFalse(ValidadorPerfil.esNombreValido(""))
        assertFalse(ValidadorPerfil.esNombreValido("   "))
    }

    @Test
    fun `esContrasenaValida retorna true si tiene longitud mayor o igual a minima`() {
        val min = ValidadorPerfil.LONGITUD_MINIMA_CONTRASENA
        
        // Exact length
        val exacta = "a".repeat(min)
        assertTrue(ValidadorPerfil.esContrasenaValida(exacta))
        
        // Shorter
        val corta = "a".repeat(min - 1)
        assertFalse(ValidadorPerfil.esContrasenaValida(corta))
        
        // Trims spaces
        val conEspacios = " " + "a".repeat(min - 1) + " "
        assertFalse(ValidadorPerfil.esContrasenaValida(conEspacios))
    }

    @Test
    fun `esCorreoValido valida estructura estandar de email`() {
        assertTrue(ValidadorPerfil.esCorreoValido("test@example.com"))
        assertTrue(ValidadorPerfil.esCorreoValido("juan.perez@duocuc.cl"))
        assertTrue(ValidadorPerfil.esCorreoValido("a@b.co"))
        
        assertFalse(ValidadorPerfil.esCorreoValido("testexample.com"))
        assertFalse(ValidadorPerfil.esCorreoValido("test@example"))
        assertFalse(ValidadorPerfil.esCorreoValido("test@.com"))
        assertFalse(ValidadorPerfil.esCorreoValido(""))
        assertFalse(ValidadorPerfil.esCorreoValido("   "))
    }
}
