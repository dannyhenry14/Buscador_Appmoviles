package com.example.buscador.model

/**
 * Modelo de dominio para representar el clima actual en la pantalla principal.
 *
 * @property temperatura Grados Celsius actuales.
 * @property viento Velocidad del viento en km/h.
 * @property condicion Descripción legible del estado del clima (ej. "Despejado").
 */
data class ClimaActual(
    val temperatura: Double,
    val viento: Double,
    val condicion: String
)