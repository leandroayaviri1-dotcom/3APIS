package com.example.tresapis.repository

import com.example.tresapis.api.ValorantApi
import com.example.tresapis.mapper.aModelo
import com.example.tresapis.modelo.Agente

class AgentesRepository(private val api: ValorantApi) {
    suspend fun agentes(): List<Agente> =
        llamar("Valorant API", "No se encontraron agentes.") {
            api.agentes().data.map { it.aModelo() }.distinctBy { it.id }.sortedBy { it.nombre }
        }
}
