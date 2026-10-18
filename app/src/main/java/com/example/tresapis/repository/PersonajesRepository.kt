package com.example.tresapis.repository

import com.example.tresapis.api.RickMortyApi
import com.example.tresapis.mapper.aModelo
import com.example.tresapis.modelo.PaginaPersonajes

class PersonajesRepository(private val api: RickMortyApi) {
    suspend fun buscar(pagina: Int, nombre: String = "", estado: String? = null): PaginaPersonajes =
        llamar("Rick and Morty API", "No hay personajes que coincidan con la búsqueda.") {
            api.personajes(pagina, nombre.trim().ifEmpty { null }, estado).aModelo()
        }
}
