package com.example.tresapis.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.tresapis.local.TresApisDatabase
import com.example.tresapis.modelo.PokemonResumen
import com.example.tresapis.repository.FavoritosRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FavoritosUiState(
    val favoritos: List<PokemonResumen> = emptyList(),
    val cargando: Boolean = true
)

// Aquí no hay red: todo sale de Room. Por eso funciona sin Internet.
class FavoritosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = FavoritosRepository(TresApisDatabase.obtener(application).favoritoDao())

    val estado: StateFlow<FavoritosUiState> = repository.observarFavoritos()
        .map { lista -> FavoritosUiState(favoritos = lista, cargando = false) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritosUiState())

    fun quitar(pokemon: PokemonResumen) {
        viewModelScope.launch { repository.quitar(pokemon.id) }
    }
}
