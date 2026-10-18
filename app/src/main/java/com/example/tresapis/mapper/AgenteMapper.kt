package com.example.tresapis.mapper

import com.example.tresapis.dto.AgenteDto
import com.example.tresapis.dto.HabilidadDto
import com.example.tresapis.dto.RolDto
import com.example.tresapis.modelo.Agente
import com.example.tresapis.modelo.Habilidad
import com.example.tresapis.modelo.Rol

// Mapper: convierte los DTO (JSON de la API) en los modelos de la app.
private val ordenSlots = listOf("Ability1", "Ability2", "Grenade", "Ultimate", "Passive")

private fun tecla(slot: String) = when (slot) {
    "Ability1" -> "Q"
    "Ability2" -> "E"
    "Grenade" -> "C"
    "Ultimate" -> "X"
    else -> "Pasiva"
}

fun AgenteDto.aModelo() = Agente(
    id = uuid,
    nombre = displayName,
    descripcion = description,
    icono = displayIcon,
    retrato = fullPortrait,
    fondo = background,
    colores = backgroundGradientColors.orEmpty(),
    rol = role?.aModelo(),
    habilidades = abilities.orEmpty()
        .sortedBy { ordenSlots.indexOf(it.slot).let { i -> if (i < 0) 99 else i } }
        .map { it.aModelo() }
)

fun RolDto.aModelo() = Rol(nombre = displayName, icono = displayIcon)

fun HabilidadDto.aModelo() = Habilidad(
    tecla = tecla(slot),
    nombre = displayName,
    descripcion = description,
    icono = displayIcon
)
