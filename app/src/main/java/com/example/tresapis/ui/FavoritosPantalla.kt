package com.example.tresapis.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tresapis.viewmodel.FavoritosViewModel

@Composable
fun FavoritosPantalla(
    viewModel: FavoritosViewModel,
    onVolver: () -> Unit,
    onPokemonClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val fondoTarjeta = Brush.verticalGradient(listOf(RojoPokebola.copy(alpha = 0.18f), Color.Transparent))

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ancho {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onVolver) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a la Pokédex")
                }
                Encabezado("GUARDADOS EN SQLITE CLOUD", "Favoritos",
                    "Se guardan en la nube: hace falta Internet.", RojoPokebola)
            }
        }
        estado.error?.let { error -> ancho { MensajeError(error, viewModel::recargar) } }
        when {
            estado.cargando && estado.favoritos.isEmpty() -> ancho { PieLista(cargando = true, hayMas = false) {} }
            estado.error != null && estado.favoritos.isEmpty() -> {}
            estado.favoritos.isEmpty() -> ancho {
                Text("Aún no tienes favoritos. Toca el corazón de un Pokémon para guardarlo.",
                    textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 32.dp))
            }
            else -> items(estado.favoritos, key = { it.id }) { pokemon ->
                TarjetaImagen(
                    imagen = pokemon.imagen, titulo = pokemon.nombre,
                    subtitulo = "#%04d".format(pokemon.id), fondo = fondoTarjeta,
                    onClick = { onPokemonClick(pokemon.id) },
                    esFavorito = true,
                    onFavoritoClick = { viewModel.quitar(pokemon) }
                )
            }
        }
    }
}
