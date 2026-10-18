package com.example.tresapis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tresapis.api.ApiClient
import com.example.tresapis.modelo.Personaje
import com.example.tresapis.repository.ApiException
import com.example.tresapis.repository.PersonajesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PersonajesUiState(
    val busqueda: String = "",
    val filtroEstado: String? = null,
    val personajes: List<Personaje> = emptyList(),
    val total: Int = 0,
    val hayMas: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null
)

class PersonajesViewModel(
    private val repository: PersonajesRepository = PersonajesRepository(ApiClient.rickMorty)
) : ViewModel() {
    private val _estado = MutableStateFlow(PersonajesUiState())
    val estado = _estado.asStateFlow()
    private var consulta: Job? = null
    private var pagina = 0
    private var nombreBuscado = ""

    init { buscar() }

    fun cambiarBusqueda(valor: String) = _estado.update { it.copy(busqueda = valor) }

    fun cambiarFiltro(estado: String?) {
        _estado.update { it.copy(filtroEstado = estado) }
        buscar()
    }

    fun buscar() {
        nombreBuscado = _estado.value.busqueda.trim()
        cargar(reiniciar = true)
    }

    fun cargarMas() {
        val actual = _estado.value
        if (!actual.cargando && actual.hayMas) cargar(reiniciar = false)
    }

    fun reintentar() = cargar(reiniciar = _estado.value.personajes.isEmpty())

    private fun cargar(reiniciar: Boolean) {
        consulta?.cancel()
        val siguiente = if (reiniciar) 1 else pagina + 1
        val filtro = _estado.value.filtroEstado
        _estado.update {
            it.copy(
                cargando = true, error = null,
                personajes = if (reiniciar) emptyList() else it.personajes,
                hayMas = !reiniciar && it.hayMas
            )
        }
        consulta = viewModelScope.launch {
            try {
                val resultado = repository.buscar(siguiente, nombreBuscado, filtro)
                pagina = siguiente
                _estado.update {
                    it.copy(
                        personajes = it.personajes + resultado.personajes,
                        total = resultado.total,
                        hayMas = resultado.hayMas,
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(error = e.message, cargando = false, total = 0) }
            } catch (e: Exception) {
                _estado.update { it.copy(error = "No se pudieron mostrar los personajes.", cargando = false) }
            }
        }
    }
}
