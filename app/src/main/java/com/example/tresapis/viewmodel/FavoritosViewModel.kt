package com.example.tresapis.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tresapis.modelo.PokemonResumen
import com.example.tresapis.repository.ApiException
import com.example.tresapis.repository.FavoritosRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FavoritosUiState(
    val favoritos: List<PokemonResumen> = emptyList(),
    val cargando: Boolean = true,
    val error: String? = null
)

// Los favoritos se leen de SQLite Cloud: hace falta Internet.
class FavoritosViewModel(
    private val repository: FavoritosRepository = FavoritosRepository.compartido
) : ViewModel() {
    private val _estado = MutableStateFlow(FavoritosUiState())

    val estado: StateFlow<FavoritosUiState> = combine(repository.observarFavoritos(), _estado) { lista, propio ->
        propio.copy(favoritos = lista)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritosUiState())

    init { recargar() }

    fun recargar() = ejecutar { repository.recargar() }

    fun quitar(pokemon: PokemonResumen) = ejecutar { repository.quitar(pokemon.id) }

    private fun ejecutar(accion: suspend () -> Unit) {
        _estado.update { it.copy(cargando = true, error = null) }
        viewModelScope.launch {
            try {
                accion()
                _estado.update { it.copy(cargando = false) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: ApiException) {
                _estado.update { it.copy(error = e.message, cargando = false) }
            } catch (e: Exception) {
                _estado.update { it.copy(error = "No se pudieron leer los favoritos.", cargando = false) }
            }
        }
    }
}
