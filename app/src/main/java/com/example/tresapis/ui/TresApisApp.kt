package com.example.tresapis.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tresapis.viewmodel.AgentesViewModel
import com.example.tresapis.viewmodel.PersonajesViewModel
import com.example.tresapis.viewmodel.PokemonViewModel

private enum class Seccion(val titulo: String, val icono: ImageVector) {
    RICK_MORTY("Rick & Morty", Icons.Default.Face),
    POKEMON("Pokémon", Icons.Default.Star),
    VALORANT("Valorant", Icons.Default.Person)
}

@Composable
fun TresApisApp() {
    // Los ViewModels viven en la Activity, así cada pestaña conserva sus datos al cambiar.
    val personajes: PersonajesViewModel = viewModel()
    val pokemon: PokemonViewModel = viewModel()
    val agentes: AgentesViewModel = viewModel()
    var actual by rememberSaveable { mutableStateOf(Seccion.RICK_MORTY) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                Seccion.entries.forEach { seccion ->
                    NavigationBarItem(
                        selected = actual == seccion,
                        onClick = { actual = seccion },
                        icon = { Icon(seccion.icono, contentDescription = null) },
                        label = { Text(seccion.titulo) }
                    )
                }
            }
        }
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (actual) {
            Seccion.RICK_MORTY -> PersonajesPantalla(personajes, modifier)
            Seccion.POKEMON -> PokemonPantalla(pokemon, modifier)
            Seccion.VALORANT -> AgentesPantalla(agentes, modifier)
        }
    }
}
