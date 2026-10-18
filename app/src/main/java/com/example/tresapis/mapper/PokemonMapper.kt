package com.example.tresapis.mapper

import com.example.tresapis.dto.PokemonDetalleDto
import com.example.tresapis.dto.PokemonResumenDto
import com.example.tresapis.dto.RespuestaPokemonDto
import com.example.tresapis.modelo.Estadistica
import com.example.tresapis.modelo.PaginaPokemon
import com.example.tresapis.modelo.PokemonDetalle
import com.example.tresapis.modelo.PokemonResumen

// Mapper: convierte los DTO (JSON de la API) en los modelos de la app.
private val nombresStats = mapOf(
    "hp" to "PS", "attack" to "Ataque", "defense" to "Defensa",
    "special-attack" to "At. esp.", "special-defense" to "Def. esp.", "speed" to "Velocidad"
)

private fun urlArtwork(id: Int) =
    "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$id.png"

/** "mr-mime" -> "Mr Mime" */
private fun nombreLegible(nombre: String) = nombre.split('-').joinToString(" ") { it.capitalizar() }

fun RespuestaPokemonDto.aModelo() = PaginaPokemon(
    pokemon = results.map { it.aModelo() },
    total = count,
    hayMas = next != null
)

fun PokemonResumenDto.aModelo(): PokemonResumen {
    // La lista no trae el número; viene al final de la url: .../pokemon/25/
    val id = url.trimEnd('/').substringAfterLast('/').toIntOrNull() ?: 0
    return PokemonResumen(id = id, nombre = nombreLegible(name), imagen = urlArtwork(id))
}

fun PokemonDetalleDto.aModelo() = PokemonDetalle(
    id = id,
    nombre = nombreLegible(name),
    imagen = sprites?.other?.officialArtwork?.frontDefault ?: sprites?.frontDefault ?: urlArtwork(id),
    // La API entrega altura en decímetros y peso en hectogramos.
    alturaMetros = height / 10.0,
    pesoKg = weight / 10.0,
    tipos = types.map { it.type.name },
    habilidades = abilities.map { nombreLegible(it.ability.name) },
    estadisticas = stats.map { Estadistica(nombresStats[it.stat.name] ?: it.stat.name, it.baseStat) }
)

fun PokemonDetalle.aResumen() = PokemonResumen(id = id, nombre = nombre, imagen = imagen)
