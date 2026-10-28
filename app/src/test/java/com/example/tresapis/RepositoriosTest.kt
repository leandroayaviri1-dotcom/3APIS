package com.example.tresapis

import com.example.tresapis.repository.*
import com.example.tresapis.modelo.Estadistica
import com.example.tresapis.modelo.PokemonResumen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RepositoriosTest {
    private lateinit var server: MockWebServer

    @Before fun preparar() { server = MockWebServer(); server.start() }

    @After fun cerrar() { server.shutdown() }

    private inline fun <reified T> servicio(): T = Retrofit.Builder().baseUrl(server.url("/"))
        .addConverterFactory(GsonConverterFactory.create()).build().create(T::class.java)

    private fun responder(json: String, codigo: Int = 200) =
        server.enqueue(MockResponse().setResponseCode(codigo).setBody(json).addHeader("Content-Type", "application/json"))

    @Test fun personajesEnviaFiltrosYConvierteJson() = runBlocking {
        responder("""{"info":{"count":1,"pages":1,"next":null},"results":[{"id":1,"name":"Rick Sanchez",
            "status":"Alive","species":"Human","type":"","gender":"Male","origin":{"name":"Earth (C-137)"},
            "location":{"name":"Citadel of Ricks"},"image":"https://x/1.jpeg","episode":["e1","e2"]}]}""")
        val r = PersonajesRepository(servicio()).buscar(2, "  rick ", "alive")
        assertEquals("/character?page=2&name=rick&status=alive", server.takeRequest().path)
        assertEquals("Rick Sanchez", r.personajes.single().nombre)
        assertFalse(r.hayMas)
    }

    @Test fun personajeInexistenteDaMensajeClaro() = runBlocking {
        responder("""{"error":"There is nothing here"}""", 404)
        val e = runCatching { PersonajesRepository(servicio()).buscar(1, "zzz") }.exceptionOrNull()
        assertEquals("No hay personajes que coincidan con la búsqueda.", e?.message)
    }

    @Test fun pokemonListaCalculaIdEImagen() = runBlocking {
        responder("""{"count":1351,"next":"n","results":[{"name":"pikachu","url":"https://pokeapi.co/api/v2/pokemon/25/"}]}""")
        val r = PokemonRepository(servicio()).listar(desde = 30)
        assertEquals("/pokemon?limit=30&offset=30", server.takeRequest().path)
        assertEquals(25, r.pokemon.single().id)
        assertTrue(r.pokemon.single().imagen.endsWith("/official-artwork/25.png"))
    }

    @Test fun favoritoSeGuardaEnSqliteCloudYSeVuelveALeer() = runBlocking {
        responder("""{"data":{"ok":true}}""")
        responder("""{"data":[{"id":25,"nombre":"Pikachu","imagen":"img","agregadoEn":1}],"metadata":{}}""")
        val repo = FavoritosRepository(servicio())
        repo.agregar(PokemonResumen(25, "Pikachu", "img"))
        val envio = server.takeRequest()
        assertEquals("POST", envio.method)
        assertEquals("/agregar_favorito", envio.path)
        assertTrue(envio.body.readUtf8().contains("\"nombre\":\"Pikachu\""))
        assertEquals("/favoritos", server.takeRequest().path)
        assertEquals(setOf(25), repo.observarIds().first())
    }

    @Test fun favoritosSinPermisoDaMensajeClaro() = runBlocking {
        responder("""{"errors":[{"status":"401"}]}""", 401)
        val e = runCatching { FavoritosRepository(servicio()).recargar() }.exceptionOrNull()
        assertEquals("SQLite Cloud no pudo responder (HTTP 401). Intenta nuevamente.", e?.message)
    }

    @Test fun pokemonDetalleNormalizaNombreYLeeCamposConGuion() = runBlocking {
        responder("""{"id":25,"name":"pikachu","height":4,"weight":60,
            "types":[{"type":{"name":"electric"}}],"stats":[{"base_stat":35,"stat":{"name":"hp"}}],
            "abilities":[{"ability":{"name":"static"}}],
            "sprites":{"front_default":"f","other":{"official-artwork":{"front_default":"art"}}}}""")
        val p = PokemonRepository(servicio()).detalle("  Pikachu ")
        assertEquals("/pokemon/pikachu", server.takeRequest().path)
        assertEquals(Estadistica("PS", 35), p.estadisticas.single())
        assertEquals("art", p.imagen)
    }

    @Test fun agentesSeOrdenanYSinDuplicados() = runBlocking {
        responder("""{"status":200,"data":[
            {"uuid":"2","displayName":"Sova","description":"d","abilities":[]},
            {"uuid":"1","displayName":"Jett","description":"d","role":{"displayName":"Duelista"},"abilities":[]},
            {"uuid":"2","displayName":"Sova","description":"d","abilities":[]}]}""")
        val agentes = AgentesRepository(servicio()).agentes()
        assertEquals("/agents?isPlayableCharacter=true&language=es-ES", server.takeRequest().path)
        assertEquals(listOf("Jett", "Sova"), agentes.map { it.nombre })
    }

    @Test fun errorDelServidorIndicaElServicio() = runBlocking {
        responder("{}", 500)
        val e = runCatching { AgentesRepository(servicio()).agentes() }.exceptionOrNull()
        assertTrue(e is ApiException)
        assertTrue(e!!.message!!.startsWith("Valorant API no pudo responder (HTTP 500)"))
    }
}
