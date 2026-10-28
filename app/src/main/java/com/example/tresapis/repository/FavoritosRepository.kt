package com.example.tresapis.repository

import com.example.tresapis.api.ApiClient
import com.example.tresapis.api.SqliteCloudApi
import com.example.tresapis.dto.IdFavoritoDto
import com.example.tresapis.mapper.aFavoritoDto
import com.example.tresapis.mapper.aResumen
import com.example.tresapis.modelo.PokemonResumen
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

// Los favoritos viven en SQLite Cloud. Se guarda aquí la última lista leída para que
// la Pokédex y la pantalla de Favoritos vean siempre lo mismo.
class FavoritosRepository(private val api: SqliteCloudApi) {
    private val favoritos = MutableStateFlow<List<PokemonResumen>>(emptyList())

    fun observarFavoritos(): Flow<List<PokemonResumen>> = favoritos.asStateFlow()

    fun observarIds(): Flow<Set<Int>> = favoritos.map { lista -> lista.map { it.id }.toSet() }

    suspend fun recargar() {
        favoritos.value = llamar("SQLite Cloud", "No se encontró la función \"favoritos\".") {
            api.favoritos().data.map { it.aResumen() }
        }
    }

    // Después de cada cambio se vuelve a leer la tabla: así la lista es la que hay en la nube.
    suspend fun agregar(pokemon: PokemonResumen) {
        llamar("SQLite Cloud", "No se encontró la función \"agregar_favorito\".") {
            api.agregarFavorito(pokemon.aFavoritoDto())
        }
        recargar()
    }

    suspend fun quitar(id: Int) {
        llamar("SQLite Cloud", "No se encontró la función \"quitar_favorito\".") {
            api.quitarFavorito(IdFavoritoDto(id))
        }
        recargar()
    }

    companion object {
        // Una sola instancia para toda la app: los dos ViewModels comparten la misma lista.
        val compartido: FavoritosRepository by lazy { FavoritosRepository(ApiClient.sqliteCloud) }
    }
}
