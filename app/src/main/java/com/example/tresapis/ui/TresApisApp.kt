package com.example.tresapis.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tresapis.viewmodel.AgentesViewModel
import com.example.tresapis.viewmodel.AuthViewModel
import com.example.tresapis.viewmodel.FavoritosViewModel
import com.example.tresapis.viewmodel.PersonajesViewModel
import com.example.tresapis.viewmodel.PokemonViewModel

private enum class Seccion(val titulo: String, val icono: ImageVector) {
    RICK_MORTY("Rick & Morty", Icons.Default.Face),
    POKEMON("Pokémon", Icons.Default.Star),
    VALORANT("Valorant", Icons.Default.Person)
}

// Sin sesión se ve el login; con sesión, las pestañas. Al entrar o salir, `usuario` cambia
// y esta función se vuelve a dibujar sola: el AuthViewModel no sabe nada de pantallas.
@Composable
fun TresApisApp(authViewModel: AuthViewModel = viewModel()) {
    val usuario by authViewModel.usuario.collectAsStateWithLifecycle()

    when (val actual = usuario) {
        null -> Scaffold { padding -> LoginPantalla(authViewModel, Modifier.padding(padding)) }
        else -> Pestanas(correo = actual.email, onCerrarSesion = authViewModel::cerrarSesion)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Pestanas(correo: String, onCerrarSesion: () -> Unit) {
    // Los ViewModels viven en la Activity, así cada pestaña conserva sus datos al cambiar.
    val personajes: PersonajesViewModel = viewModel()
    val pokemon: PokemonViewModel = viewModel()
    val agentes: AgentesViewModel = viewModel()
    val favoritos: FavoritosViewModel = viewModel()
    var actual by rememberSaveable { mutableStateOf(Seccion.RICK_MORTY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(correo, style = MaterialTheme.typography.bodyMedium) },
                actions = {
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
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
            Seccion.POKEMON -> PokemonPantalla(pokemon, favoritos, modifier)
            Seccion.VALORANT -> AgentesPantalla(agentes, modifier)
        }
    }
}
