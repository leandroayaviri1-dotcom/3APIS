package com.example.tresapis.api

import com.example.tresapis.dto.FavoritoDto
import com.example.tresapis.dto.IdFavoritoDto
import com.example.tresapis.dto.RespuestaSqliteCloud
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

// Cada método es una Edge Function de SQLite Cloud; el SQL está escrito en el panel.
interface SqliteCloudApi {
    @GET("favoritos")
    suspend fun favoritos(): RespuestaSqliteCloud<List<FavoritoDto>>

    @POST("agregar_favorito")
    suspend fun agregarFavorito(@Body favorito: FavoritoDto): RespuestaSqliteCloud<Any?>

    @POST("quitar_favorito")
    suspend fun quitarFavorito(@Body id: IdFavoritoDto): RespuestaSqliteCloud<Any?>
}
