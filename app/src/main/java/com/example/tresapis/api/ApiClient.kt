package com.example.tresapis.api

import com.example.tresapis.BuildConfig
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {
    private val client by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .callTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    private fun retrofit(baseUrl: String) = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val rickMorty: RickMortyApi by lazy {
        retrofit("https://rickandmortyapi.com/api/").create(RickMortyApi::class.java)
    }
    val pokeApi: PokeApi by lazy {
        retrofit("https://pokeapi.co/api/v2/").create(PokeApi::class.java)
    }
    val valorant: ValorantApi by lazy {
        retrofit("https://valorant-api.com/v1/").create(ValorantApi::class.java)
    }

    // SQLite Cloud pide la API key en cada llamada; se agrega aquí para no repetirla en cada método.
    val sqliteCloud: SqliteCloudApi by lazy {
        val conClave = client.newBuilder()
            .addInterceptor { cadena ->
                cadena.proceed(
                    cadena.request().newBuilder()
                        .header("Authorization", "Bearer ${BuildConfig.SQLITECLOUD_API_KEY}")
                        .build()
                )
            }
            .build()
        Retrofit.Builder()
            .baseUrl("${BuildConfig.SQLITECLOUD_URL}/v2/functions/")
            .client(conClave)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SqliteCloudApi::class.java)
    }
}
