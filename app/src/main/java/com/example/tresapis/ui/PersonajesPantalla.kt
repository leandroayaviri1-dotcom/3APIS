package com.example.tresapis.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.tresapis.modelo.EstadoPersonaje
import com.example.tresapis.modelo.Personaje
import com.example.tresapis.viewmodel.PersonajesViewModel

private val filtrosEstado = listOf(null to "Todos", "alive" to "Vivos", "dead" to "Muertos", "unknown" to "Desconocido")

private fun colorEstado(estado: EstadoPersonaje) = when (estado) {
    EstadoPersonaje.VIVO -> Color(0xFF2E9E44)
    EstadoPersonaje.MUERTO -> Color(0xFFD32F2F)
    EstadoPersonaje.DESCONOCIDO -> Color(0xFF757575)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonajesPantalla(viewModel: PersonajesViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var seleccionado by remember { mutableStateOf<Personaje?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ancho {
            Encabezado("RICK AND MORTY API", "Personajes",
                "Explora los personajes del multiverso.", VerdePortal)
        }
        ancho {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoBusqueda(
                    estado.busqueda, viewModel::cambiarBusqueda, viewModel::buscar, "Buscar por nombre",
                    onLimpiar = { viewModel.cambiarBusqueda(""); viewModel.buscar() }
                )
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    filtrosEstado.forEach { (valor, texto) ->
                        FilterChip(
                            selected = estado.filtroEstado == valor,
                            onClick = { viewModel.cambiarFiltro(valor) },
                            label = { Text(texto) }
                        )
                    }
                }
                if (estado.total > 0) Text("${estado.total} personajes encontrados",
                    style = MaterialTheme.typography.bodySmall)
            }
        }
        estado.error?.let { error -> ancho { MensajeError(error, viewModel::reintentar) } }
        items(estado.personajes, key = { it.id }) { personaje ->
            TarjetaImagen(
                imagen = personaje.imagen, titulo = personaje.nombre, subtitulo = personaje.especie,
                onClick = { seleccionado = personaje },
                insignia = { Insignia(personaje.estado.texto, colorEstado(personaje.estado)) }
            )
        }
        ancho { PieLista(estado.cargando, estado.hayMas && estado.error == null, viewModel::cargarMas) }
    }

    seleccionado?.let { personaje ->
        ModalBottomSheet(onDismissRequest = { seleccionado = null }) {
            DetallePersonaje(personaje)
        }
    }
}

@Composable
private fun DetallePersonaje(personaje: Personaje) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = 32.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = personaje.imagen, contentDescription = personaje.nombre,
                modifier = Modifier.size(110.dp).clip(RoundedCornerShape(20.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(personaje.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Insignia(personaje.estado.texto, colorEstado(personaje.estado))
            }
        }
        Spacer(Modifier.height(16.dp))
        FilaDato("Especie", personaje.especie)
        personaje.tipo?.let { FilaDato("Tipo", it) }
        FilaDato("Género", personaje.genero)
        FilaDato("Origen", personaje.origen)
        FilaDato("Ubicación", personaje.ubicacion)
        FilaDato("Episodios", "${personaje.episodios}")
    }
}
