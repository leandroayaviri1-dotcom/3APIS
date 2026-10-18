package com.example.tresapis.mapper

import com.example.tresapis.dto.PersonajeDto
import com.example.tresapis.dto.RespuestaPersonajesDto
import com.example.tresapis.modelo.EstadoPersonaje
import com.example.tresapis.modelo.PaginaPersonajes
import com.example.tresapis.modelo.Personaje

// Mapper: convierte el DTO (JSON de la API) en el modelo de la app.
fun RespuestaPersonajesDto.aModelo() = PaginaPersonajes(
    personajes = results.map { it.aModelo() },
    total = info.count,
    hayMas = info.next != null
)

fun PersonajeDto.aModelo() = Personaje(
    id = id,
    nombre = name,
    estado = when (status.lowercase()) {
        "alive" -> EstadoPersonaje.VIVO
        "dead" -> EstadoPersonaje.MUERTO
        else -> EstadoPersonaje.DESCONOCIDO
    },
    especie = species,
    tipo = type?.takeIf { it.isNotBlank() },
    genero = when (gender.lowercase()) {
        "male" -> "Masculino"
        "female" -> "Femenino"
        "genderless" -> "Sin género"
        else -> "Desconocido"
    },
    origen = origin.name.capitalizar(),
    ubicacion = location.name.capitalizar(),
    imagen = image,
    episodios = episode.size
)

internal fun String.capitalizar() = replaceFirstChar { it.uppercase() }
