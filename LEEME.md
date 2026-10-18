# TresApis — Android Studio

App en Kotlin y Jetpack Compose que consume **tres APIs públicas** (ninguna necesita clave). Cada una tiene su propia pestaña en la barra inferior.

| Pestaña | API | Qué muestra |
|---|---|---|
| Rick & Morty | `https://rickandmortyapi.com/api/character` | Personajes con búsqueda por nombre, filtro por estado (vivo/muerto/desconocido) y paginación. |
| Pokémon | `https://pokeapi.co/api/v2/pokemon` | Pokédex paginada; búsqueda por nombre o número; detalle con tipos, altura, peso, habilidades y estadísticas. |
| Valorant | `https://valorant-api.com/v1/agents` | Agentes en español, filtro por nombre y rol; detalle con descripción y habilidades (Q, E, C, X). |

## Abrir y ejecutar

1. En Android Studio elige **Open** y selecciona la carpeta **TresApis** (la que contiene `settings.gradle.kts`).
2. Usa **JDK 17 o superior** como Gradle JDK.
3. Espera la sincronización de Gradle y ejecuta el módulo `app` en un emulador o teléfono con Android 8.0 (API 26) o superior, con Internet.

## Organización

Código en `app/src/main/java/com/example/tresapis/`. Cada API tiene una clase en cada capa:

```
API  →  Repository  →  (DTO → Mapper → Modelo)  →  ViewModel  →  View
```

| Capa | Rick & Morty | Pokémon | Valorant |
|---|---|---|---|
| `api/` (Retrofit) | `RickMortyApi` | `PokeApi` | `ValorantApi` |
| `repository/` | `PersonajesRepository` | `PokemonRepository` | `AgentesRepository` |
| `dto/` (JSON tal cual) | `PersonajeDto` | `PokemonDetalleDto`, `PokemonResumenDto` | `AgenteDto` |
| `mapper/` (DTO → modelo) | `PersonajeMapper` | `PokemonMapper` | `AgenteMapper` |
| `modelo/` (lo que usa la app) | `Personaje` | `PokemonDetalle`, `PokemonResumen` | `Agente` |
| `viewmodel/` | `PersonajesViewModel` | `PokemonViewModel` | `AgentesViewModel` |
| `ui/` (View) | `PersonajesPantalla` | `PokemonPantalla` | `AgentesPantalla` |

- `api/ApiClient.kt` crea un cliente Retrofit por API y comparte el mismo OkHttp.
- `repository/ApiException.kt` traduce los errores (sin Internet, 404, tiempo agotado…) a mensajes en español.
- `ui/` también contiene los componentes compartidos, el tema y `TresApisApp.kt`, que tiene la navegación inferior.

## Pruebas

`app/src/test/.../RepositoriosTest.kt` usa MockWebServer para comprobar los parámetros enviados a cada API, la conversión del JSON y los mensajes de error. `MappersTest.kt` comprueba que cada mapper convierta bien el DTO en el modelo. Se ejecutan con:

```
./gradlew :app:testDebugUnitTest
```
