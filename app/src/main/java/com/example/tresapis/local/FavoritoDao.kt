package com.example.tresapis.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

// DAO = las operaciones permitidas sobre la tabla. Room escribe la implementación.
@Dao
interface FavoritoDao {
    // Flow: Room vuelve a emitir la lista cada vez que la tabla cambia.
    @Query("SELECT * FROM favoritos ORDER BY agregadoEn DESC")
    fun observarFavoritos(): Flow<List<FavoritoEntity>>

    // Solo los ids: basta para pintar el corazón relleno o vacío.
    @Query("SELECT id FROM favoritos")
    fun observarIdsFavoritos(): Flow<List<Int>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(favorito: FavoritoEntity)

    @Query("DELETE FROM favoritos WHERE id = :id")
    suspend fun eliminarPorId(id: Int)
}
