package com.example.tresapis.modelo

// Modelos que usa la app. Los crea AgenteMapper a partir de los DTO.
data class Agente(
    val id: String,
    val nombre: String,
    val descripcion: String,
    val icono: String?,
    val retrato: String?,
    val fondo: String?,
    /** Colores "RRGGBBAA" tal como los entrega la API. */
    val colores: List<String>,
    val rol: Rol?,
    /** Ya ordenadas: Q, E, C, X, pasiva. */
    val habilidades: List<Habilidad>
)

data class Rol(val nombre: String, val icono: String?)

data class Habilidad(
    val tecla: String,
    val nombre: String,
    val descripcion: String,
    val icono: String?
)
