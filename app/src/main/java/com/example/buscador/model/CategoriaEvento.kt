package com.example.buscador.model

/**
 * Categorías de interés por las que se puede filtrar un evento.
 *
 * @property nombreVisible Texto que verá el usuario en los filtros.
 */
enum class CategoriaEvento(val nombreVisible: String) {
    SEMINARIO("Seminario"),
    TALLER("Taller"),
    FERIA("Feria"),
    CHARLA("Charla")
}