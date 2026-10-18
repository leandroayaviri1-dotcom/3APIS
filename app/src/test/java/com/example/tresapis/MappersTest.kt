package com.example.tresapis

import com.example.tresapis.dto.*
import com.example.tresapis.mapper.aModelo
import com.example.tresapis.modelo.EstadoPersonaje
import org.junit.Assert.*
import org.junit.Test

class MappersTest {
    @Test fun personajeSeTraduceAlEspanol() {
        val dto = PersonajeDto(1, "Rick Sanchez", "Alive", "Human", "", "Male",
            LugarDto("earth (C-137)"), LugarDto("citadel of Ricks"), "img", listOf("e1", "e2"))
        val p = dto.aModelo()
        assertEquals(EstadoPersonaje.VIVO, p.estado)
        assertEquals("Masculino", p.genero)
        assertEquals("Earth (C-137)", p.origen)
        assertNull(p.tipo)
        assertEquals(2, p.episodios)
    }

    @Test fun pokemonConvierteUnidadesYNombres() {
        val dto = PokemonDetalleDto(122, "mr-mime", 13, 545, listOf(TipoSlotDto(NombreDto("psychic"))),
            listOf(StatSlotDto(40, NombreDto("hp"))), listOf(HabilidadSlotDto(NombreDto("soundproof"))), null)
        val p = dto.aModelo()
        assertEquals("Mr Mime", p.nombre)
        assertEquals(1.3, p.alturaMetros, 0.001)
        assertEquals(54.5, p.pesoKg, 0.001)
        assertEquals(listOf("Soundproof"), p.habilidades)
        assertTrue(p.imagen.endsWith("/official-artwork/122.png"))
    }

    @Test fun agenteOrdenaHabilidadesYAsignaTecla() {
        val dto = AgenteDto("1", "Jett", "d", null, null, null, null, RolDto("Duelista", null), listOf(
            HabilidadDto("Ultimate", "Tormenta de cuchillas", "", null),
            HabilidadDto("Ability1", "Corriente ascendente", "", null)
        ))
        val a = dto.aModelo()
        assertEquals(listOf("Q", "X"), a.habilidades.map { it.tecla })
        assertEquals("Duelista", a.rol?.nombre)
    }
}
