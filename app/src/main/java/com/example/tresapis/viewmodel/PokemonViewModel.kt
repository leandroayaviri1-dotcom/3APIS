package com.example.tresapis.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tresapis.api.ApiClient
import com.example.tresapis.local.TresApisDatabase
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
    /** Lo que se dibuja: la caché de Room, o el resultado de la búsqueda si hay una activa. */
    val pokemon: List<PokemonResumen> = emptyList(),
    /** null = se muestra la caché; con valor = se muestra el resultado de buscar (necesita Internet). */
    val resultadoBusqueda: List<PokemonResumen>? = null,
    val total: Int = 0,
    val hayMas: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val seleccionado: Int? = null,
    val detalle: PokemonDetalle? = null,
    val cargandoDetalle: Boolean = false,
    val errorDetalle: String? = null,
    val favoritosIds: Set<Int> = emptySet()
) {
    val detalleEsFavorito: Boolean get() = detalle != null && detalle.id in favoritosIds
}

// AndroidViewModel: recibe Application, que Room necesita para abrir la base de datos.
class PokemonViewModel(application: Application) : AndroidViewModel(application) {
    private val baseDeDatos = TresApisDatabase.obtener(application)
    private val repository = PokemonRepository(ApiClient.pokeApi, baseDeDatos.pokemonCacheDao())
    private val favoritos = FavoritosRepository(baseDeDatos.favoritoDao())

    // Lo que el ViewModel maneja por su cuenta: búsqueda, estado de la red y detalle.
    private val _estado = MutableStateFlow(PokemonUiState(cargando = true))

    // Lo que ve la pantalla = lista guardada en Room + estado propio + ids favoritos.
    val estado: StateFlow<PokemonUiState> = combine(
        repository.observarPokemon(),   // 1. la lista guardada en Room
        _estado,                        // 2. cargando / error / hay más / búsqueda / detalle
        favoritos.observarIds()         // 3. los favoritos
    ) { guardados, propio, ids ->
        propio.copy(pokemon = propio.resultadoBusqueda ?: guardados, favoritosIds = ids)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PokemonUiState(cargando = true))

    private var consulta: Job? = null
    private var consultaDetalle: Job? = null

    init {
        viewModelScope.launch {
            if (repository.hayPokemonGuardados()) {
                // Ya hay datos: se muestran al instante y NO se llama a la API.
                _estado.update { it.copy(cargando = false, hayMas = true) }
            } else {
                // Primera vez (caché vacía): hay que descargar la primera página.
                pedirSiguientePagina()
            }
        }
    }

    fun cambiarBusqueda(valor: String) = _estado.update { it.copy(busqueda = valor) }

    /** Con texto busca un Pokémon exacto en la API; sin texto vuelve a la lista guardada. */
    fun buscar() {
        val texto = _estado.value.busqueda.trim()
        if (texto.isEmpty()) return limpiarBusqueda()
        consulta?.cancel()
        _estado.update { it.copy(cargando = true, error = null, resultadoBusqueda = emptyList()) }
        consulta = viewModelScope.launch {
            try {
                val detalle = repository.detalle(texto)
                _estado.update { it.copy(resultadoBusqueda = listOf(detalle.aResumen()), cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(error = e.message, cargando = false) }
            } catch (e: Exception) {
                _estado.update { it.copy(error = "No se pudo buscar el Pokémon.", cargando = false) }
            }
        }
    }

    fun limpiarBusqueda() {
        consulta?.cancel()
        _estado.update { it.copy(busqueda = "", resultadoBusqueda = null, error = null, cargando = false) }
    }

    fun cargarMas() {
        val actual = _estado.value
        if (actual.cargando || !actual.hayMas || actual.resultadoBusqueda != null) return
        consulta = viewModelScope.launch { pedirSiguientePagina() }
    }

    fun reintentar() {
        if (_estado.value.resultadoBusqueda != null) buscar()
        else consulta = viewModelScope.launch { pedirSiguientePagina() }
    }

    // El resultado NO trae la lista: el repositorio la guarda en Room y Room la emite sola.
    private suspend fun pedirSiguientePagina() {
        _estado.update { it.copy(cargando = true, error = null) }
        try {
            val pagina = repository.cargarSiguientePagina()
            _estado.update { it.copy(total = pagina.total, hayMas = pagina.hayMas, cargando = false) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: ApiException) {
            _estado.update { it.copy(error = e.message, cargando = false) }
        } catch (e: Exception) {
            _estado.update { it.copy(error = "No se pudo mostrar la lista de Pokémon.", cargando = false) }
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

    // Si ya era favorito lo quita; si no, lo guarda. Room avisa y el corazón cambia solo.
    fun alternarFavorito(pokemon: PokemonResumen) {
        val esFavorito = pokemon.id in estado.value.favoritosIds
        viewModelScope.launch {
            if (esFavorito) favoritos.quitar(pokemon.id) else favoritos.agregar(pokemon)
        }
    }

    fun alternarFavoritoDetalle() {
        _estado.value.detalle?.let { alternarFavorito(it.aResumen()) }
    }

    fun cerrarDetalle() {
        consultaDetalle?.cancel()
        _estado.update { it.copy(seleccionado = null, detalle = null, cargandoDetalle = false, errorDetalle = null) }
    }
}
