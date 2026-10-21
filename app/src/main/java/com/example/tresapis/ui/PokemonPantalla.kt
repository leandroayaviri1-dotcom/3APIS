package com.example.tresapis.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.tresapis.modelo.PokemonDetalle
import com.example.tresapis.viewmodel.FavoritosViewModel
import com.example.tresapis.viewmodel.PokemonViewModel
import java.util.Locale

private val tipos = mapOf(
    "normal" to ("Normal" to Color(0xFF9FA19F)), "fire" to ("Fuego" to Color(0xFFE62829)),
    "water" to ("Agua" to Color(0xFF2980EF)), "grass" to ("Planta" to Color(0xFF3FA129)),
    "electric" to ("Eléctrico" to Color(0xFFDDB400)), "ice" to ("Hielo" to Color(0xFF3DCEF3)),
    "fighting" to ("Lucha" to Color(0xFFFF8000)), "poison" to ("Veneno" to Color(0xFF9141CB)),
    "ground" to ("Tierra" to Color(0xFF915121)), "flying" to ("Volador" to Color(0xFF81B9EF)),
    "psychic" to ("Psíquico" to Color(0xFFEF4179)), "bug" to ("Bicho" to Color(0xFF91A119)),
    "rock" to ("Roca" to Color(0xFFAFA981)), "ghost" to ("Fantasma" to Color(0xFF704170)),
    "dragon" to ("Dragón" to Color(0xFF5060E1)), "dark" to ("Siniestro" to Color(0xFF624D4E)),
    "steel" to ("Acero" to Color(0xFF60A1B8)), "fairy" to ("Hada" to Color(0xFFEF70EF))
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PokemonPantalla(
    viewModel: PokemonViewModel,
    favoritosViewModel: FavoritosViewModel,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var verFavoritos by rememberSaveable { mutableStateOf(false) }

    // El botón "atrás" del teléfono regresa de Favoritos a la Pokédex.
    BackHandler(enabled = verFavoritos) { verFavoritos = false }

    if (verFavoritos) {
        FavoritosPantalla(favoritosViewModel, onVolver = { verFavoritos = false },
            onPokemonClick = viewModel::seleccionar, modifier = modifier)
    } else {
        ListaPokemon(viewModel, onVerFavoritos = { verFavoritos = true }, modifier = modifier)
    }

    // El detalle se abre igual desde la Pokédex y desde Favoritos.
    if (estado.seleccionado != null) {
        ModalBottomSheet(onDismissRequest = viewModel::cerrarDetalle) {
            Box(Modifier.fillMaxWidth().padding(bottom = 32.dp), contentAlignment = Alignment.Center) {
                when {
                    estado.cargandoDetalle -> CircularProgressIndicator(Modifier.padding(32.dp))
                    estado.errorDetalle != null -> Box(Modifier.padding(20.dp)) {
                        MensajeError(estado.errorDetalle!!) { viewModel.seleccionar(estado.seleccionado!!) }
                    }
                    estado.detalle != null -> DetallePokemon(
                        estado.detalle!!, estado.detalleEsFavorito, viewModel::alternarFavoritoDetalle
                    )
                }
            }
        }
    }
}

@Composable
private fun ListaPokemon(viewModel: PokemonViewModel, onVerFavoritos: () -> Unit, modifier: Modifier) {
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
                Box(Modifier.weight(1f)) {
                    Encabezado("POKÉAPI", "Pokédex", "Toca un Pokémon para ver sus estadísticas.", RojoPokebola)
                }
                FilledTonalIconButton(onClick = onVerFavoritos) {
                    Icon(Icons.Default.Favorite, contentDescription = "Ver favoritos", tint = Color(0xFFE53935))
                }
            }
        }
        ancho {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoBusqueda(
                    estado.busqueda, viewModel::cambiarBusqueda, viewModel::buscar,
                    "Nombre o número exacto", onLimpiar = viewModel::limpiarBusqueda
                )
                if (estado.resultadoBusqueda == null && estado.total > 0) Text("Mostrando ${estado.pokemon.size} de ${estado.total} Pokémon",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        // Sin nada que mostrar el error va arriba; con la lista guardada va al final, junto a "Cargar más".
        if (estado.pokemon.isEmpty()) estado.error?.let { error -> ancho { MensajeError(error, viewModel::reintentar) } }
        items(estado.pokemon, key = { it.id }) { pokemon ->
            TarjetaImagen(
                imagen = pokemon.imagen, titulo = pokemon.nombre,
                subtitulo = "#%04d".format(pokemon.id), fondo = fondoTarjeta,
                onClick = { viewModel.seleccionar(pokemon.id) },
                esFavorito = pokemon.id in estado.favoritosIds,
                onFavoritoClick = { viewModel.alternarFavorito(pokemon) }
            )
        }
        if (estado.pokemon.isNotEmpty()) estado.error?.let { error ->
            ancho { MensajeError(error, viewModel::reintentar) }
        }
        ancho {
            PieLista(estado.cargando, estado.hayMas && estado.error == null && estado.resultadoBusqueda == null,
                viewModel::cargarMas)
        }
    }
}

@Composable
private fun DetallePokemon(pokemon: PokemonDetalle, esFavorito: Boolean, onFavoritoClick: () -> Unit) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AsyncImage(model = pokemon.imagen, contentDescription = pokemon.nombre, modifier = Modifier.size(180.dp))
        Text("#%04d".format(pokemon.id), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(pokemon.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            BotonFavorito(esFavorito, onFavoritoClick)
        }
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            pokemon.tipos.forEach { tipo ->
                val (nombre, color) = tipos[tipo] ?: (tipo.capitalizar() to Color.Gray)
                Insignia(nombre, color)
            }
        }
        Spacer(Modifier.height(16.dp))
        FilaDato("Altura", String.format(Locale.getDefault(), "%.1f m", pokemon.alturaMetros))
        FilaDato("Peso", String.format(Locale.getDefault(), "%.1f kg", pokemon.pesoKg))
        FilaDato("Habilidades", pokemon.habilidades.joinToString(", "))
        Spacer(Modifier.height(12.dp))
        Text("Estadísticas base", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
        pokemon.estadisticas.forEach { stat ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stat.nombre, modifier = Modifier.width(90.dp),
                    style = MaterialTheme.typography.bodySmall)
                Text("${stat.valor}", modifier = Modifier.width(36.dp), fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodySmall)
                LinearProgressIndicator(
                    progress = { (stat.valor / 255f).coerceIn(0f, 1f) },
                    modifier = Modifier.weight(1f).height(8.dp),
                    color = RojoPokebola
                )
            }
        }
    }
}
