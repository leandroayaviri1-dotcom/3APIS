package com.example.tresapis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tresapis.api.ApiClient
import com.example.tresapis.mapper.aResumen
import com.example.tresapis.modelo.PokemonDetalle
import com.example.tresapis.modelo.PokemonResumen
import com.example.tresapis.repository.ApiException
import com.example.tresapis.repository.FavoritosRepository
import com.example.tresapis.repository.PokemonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PokemonUiState(
    val busqueda: String = "",
    val pokemon: List<PokemonResumen> = emptyList(),
    val total: Int = 0,
    val hayMas: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val seleccionado: Int? = null,
    val detalle: PokemonDetalle? = null,
    val cargandoDetalle: Boolean = false,
    val errorDetalle: String? = null,
    val favoritosIds: Set<Int> = emptySet(),
    /** Error al guardar o quitar un favorito en SQLite Cloud. */
    val errorFavorito: String? = null
) {
    val detalleEsFavorito: Boolean get() = detalle != null && detalle.id in favoritosIds
}

// La lista viene siempre de PokéAPI; los favoritos, de SQLite Cloud.
class PokemonViewModel(
    private val repository: PokemonRepository = PokemonRepository(ApiClient.pokeApi),
    private val favoritos: FavoritosRepository = FavoritosRepository.compartido
) : ViewModel() {
    private val _estado = MutableStateFlow(PokemonUiState())

    // Lo que ve la pantalla = estado propio + ids de los favoritos guardados en la nube.
    val estado: StateFlow<PokemonUiState> = combine(_estado, favoritos.observarIds()) { propio, ids ->
        propio.copy(favoritosIds = ids)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PokemonUiState())

    private var consulta: Job? = null
    private var consultaDetalle: Job? = null

    init {
        cargarLista(reiniciar = true)
        recargarFavoritos()
    }

    fun cambiarBusqueda(valor: String) = _estado.update { it.copy(busqueda = valor) }

    /** Con texto busca un Pokémon exacto; sin texto vuelve a la lista completa. */
    fun buscar() {
        val texto = _estado.value.busqueda.trim()
        if (texto.isEmpty()) return cargarLista(reiniciar = true)
        consulta?.cancel()
        _estado.update { it.copy(cargando = true, error = null, pokemon = emptyList(), hayMas = false) }
        consulta = viewModelScope.launch {
            try {
                val detalle = repository.detalle(texto)
                _estado.update { it.copy(pokemon = listOf(detalle.aResumen()), total = 1, cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(error = e.message, cargando = false, total = 0) }
            } catch (e: Exception) {
                _estado.update { it.copy(error = "No se pudo buscar el Pokémon.", cargando = false) }
            }
        }
    }

    fun limpiarBusqueda() {
        _estado.update { it.copy(busqueda = "") }
        cargarLista(reiniciar = true)
    }

    fun cargarMas() {
        val actual = _estado.value
        if (!actual.cargando && actual.hayMas) cargarLista(reiniciar = false)
    }

    fun reintentar() {
        if (_estado.value.busqueda.isNotBlank()) buscar()
        else cargarLista(reiniciar = _estado.value.pokemon.isEmpty())
    }

    private fun cargarLista(reiniciar: Boolean) {
        consulta?.cancel()
        val desde = if (reiniciar) 0 else _estado.value.pokemon.size
        _estado.update {
            it.copy(
                cargando = true, error = null,
                pokemon = if (reiniciar) emptyList() else it.pokemon,
                hayMas = !reiniciar && it.hayMas
            )
        }
        consulta = viewModelScope.launch {
            try {
                val pagina = repository.listar(desde)
                _estado.update {
                    it.copy(
                        pokemon = it.pokemon + pagina.pokemon,
                        total = pagina.total,
                        hayMas = pagina.hayMas,
                        cargando = false
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(error = e.message, cargando = false) }
            } catch (e: Exception) {
                _estado.update { it.copy(error = "No se pudo mostrar la lista de Pokémon.", cargando = false) }
            }
        }
    }

    fun seleccionar(id: Int) {
        consultaDetalle?.cancel()
        _estado.update { it.copy(seleccionado = id, detalle = null, cargandoDetalle = true, errorDetalle = null) }
        consultaDetalle = viewModelScope.launch {
            try {
                val detalle = repository.detalle(id.toString())
                _estado.update { it.copy(detalle = detalle, cargandoDetalle = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(errorDetalle = e.message, cargandoDetalle = false) }
            } catch (e: Exception) {
                _estado.update { it.copy(errorDetalle = "No se pudo cargar el detalle.", cargandoDetalle = false) }
            }
        }
    }

    // Si ya era favorito lo quita; si no, lo guarda en SQLite Cloud. El corazón cambia al recargar la lista.
    fun alternarFavorito(pokemon: PokemonResumen) {
        val esFavorito = pokemon.id in estado.value.favoritosIds
        operarFavoritos { if (esFavorito) favoritos.quitar(pokemon.id) else favoritos.agregar(pokemon) }
    }

    fun alternarFavoritoDetalle() {
        _estado.value.detalle?.let { alternarFavorito(it.aResumen()) }
    }

    fun recargarFavoritos() = operarFavoritos { favoritos.recargar() }

    private fun operarFavoritos(accion: suspend () -> Unit) {
        _estado.update { it.copy(errorFavorito = null) }
        viewModelScope.launch {
            try {
                accion()
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(errorFavorito = e.message) }
            } catch (e: Exception) {
                _estado.update { it.copy(errorFavorito = "No se pudo guardar el favorito.") }
            }
        }
    }

    fun cerrarDetalle() {
        consultaDetalle?.cancel()
        _estado.update { it.copy(seleccionado = null, detalle = null, cargandoDetalle = false, errorDetalle = null) }
    }
}
