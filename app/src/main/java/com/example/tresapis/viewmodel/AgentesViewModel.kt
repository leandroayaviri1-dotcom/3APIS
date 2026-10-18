package com.example.tresapis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tresapis.api.ApiClient
import com.example.tresapis.modelo.Agente
import com.example.tresapis.repository.AgentesRepository
import com.example.tresapis.repository.ApiException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AgentesUiState(
    val agentes: List<Agente> = emptyList(),
    val busqueda: String = "",
    val rol: String? = null,
    val cargando: Boolean = false,
    val error: String? = null
) {
    val roles: List<String> get() = agentes.mapNotNull { it.rol?.nombre }.distinct().sorted()

    // La API devuelve todos los agentes de una vez, así que el filtro es local.
    val visibles: List<Agente> get() = agentes.filter {
        (rol == null || it.rol?.nombre == rol) &&
            it.nombre.contains(busqueda.trim(), ignoreCase = true)
    }
}

class AgentesViewModel(
    private val repository: AgentesRepository = AgentesRepository(ApiClient.valorant)
) : ViewModel() {
    private val _estado = MutableStateFlow(AgentesUiState())
    val estado = _estado.asStateFlow()
    private var consulta: Job? = null

    init { cargar() }

    fun cambiarBusqueda(valor: String) = _estado.update { it.copy(busqueda = valor) }

    fun cambiarRol(rol: String?) = _estado.update { it.copy(rol = rol) }

    fun cargar() {
        consulta?.cancel()
        _estado.update { it.copy(cargando = true, error = null) }
        consulta = viewModelScope.launch {
            try {
                val agentes = repository.agentes()
                _estado.update { it.copy(agentes = agentes, cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(error = e.message, cargando = false) }
            } catch (e: Exception) {
                _estado.update { it.copy(error = "No se pudieron mostrar los agentes.", cargando = false) }
            }
        }
    }
}
