package com.example.tresapis.modelo

// Modelo que usa la app (ViewModel y pantalla). Lo crea PersonajeMapper a partir del DTO.
enum class EstadoPersonaje(val texto: String) {
    VIVO("Vivo"), MUERTO("Muerto"), DESCONOCIDO("Desconocido")
}

data class Personaje(
    val id: Int,
    val nombre: String,
    val estado: EstadoPersonaje,
    val especie: String,
    val tipo: String?,
    val genero: String,
    val origen: String,
    val ubicacion: String,
    val imagen: String,
    val episodios: Int
)

data class PaginaPersonajes(
    val personajes: List<Personaje>,
    val total: Int,
    val hayMas: Boolean
)
