package com.example.tresapis.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonCacheDao {
    // La API entrega los Pokémon por número ascendente: ordenar por id conserva ese orden.
    @Query("SELECT * FROM pokemon_cache ORDER BY id ASC")
    fun observarTodos(): Flow<List<PokemonCacheEntity>>

    // Cuántos hay guardados: también es el "offset" de la siguiente página de la API.
    @Query("SELECT COUNT(*) FROM pokemon_cache")
    suspend fun contar(): Int

    // Una sola llamada = una sola transacción: la página se guarda completa o no se guarda.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(pokemon: List<PokemonCacheEntity>)
}
