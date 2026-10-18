package com.example.tresapis.dto

import com.google.gson.annotations.SerializedName

// DTO = copia exacta del JSON de https://pokeapi.co/api/v2/pokemon
data class RespuestaPokemonDto(
    val count: Int,
    val next: String?,
    val results: List<PokemonResumenDto>
)

data class PokemonResumenDto(val name: String, val url: String)

// https://pokeapi.co/api/v2/pokemon/{nombre}
data class PokemonDetalleDto(
    val id: Int,
    val name: String,
    val height: Int,
    val weight: Int,
    val types: List<TipoSlotDto>,
    val stats: List<StatSlotDto>,
    val abilities: List<HabilidadSlotDto>,
    val sprites: SpritesDto?
)

data class NombreDto(val name: String)
data class TipoSlotDto(val type: NombreDto)
data class StatSlotDto(@SerializedName("base_stat") val baseStat: Int, val stat: NombreDto)
data class HabilidadSlotDto(val ability: NombreDto)
data class SpritesDto(
    @SerializedName("front_default") val frontDefault: String?,
    val other: OtrosSpritesDto?
)
data class OtrosSpritesDto(@SerializedName("official-artwork") val officialArtwork: ArtworkDto?)
data class ArtworkDto(@SerializedName("front_default") val frontDefault: String?)
