package com.example.tresapis.ui

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.tresapis.modelo.Agente
import com.example.tresapis.viewmodel.AgentesViewModel

/** La API entrega los colores como "RRGGBBAA". */
private fun colorHex(hex: String): Color? = hex.takeIf { it.length == 8 }?.toLongOrNull(16)?.let {
    Color(red = (it shr 24 and 0xFF).toInt(), green = (it shr 16 and 0xFF).toInt(),
        blue = (it shr 8 and 0xFF).toInt(), alpha = (it and 0xFF).toInt())
}

private fun degradado(agente: Agente): Brush {
    val colores = agente.colores.mapNotNull(::colorHex).take(3)
    return Brush.verticalGradient(if (colores.size >= 2) colores else listOf(RojoValorant, Color(0xFF0F1923)))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentesPantalla(viewModel: AgentesViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var seleccionado by remember { mutableStateOf<Agente?>(null) }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(150.dp),
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ancho { Encabezado("VALORANT API", "Agentes", "Conoce a cada agente y sus habilidades.", RojoValorant) }
        ancho {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CampoBusqueda(estado.busqueda, viewModel::cambiarBusqueda, {}, "Filtrar por nombre")
                Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = estado.rol == null, onClick = { viewModel.cambiarRol(null) },
                        label = { Text("Todos") })
                    estado.roles.forEach { rol ->
                        FilterChip(selected = estado.rol == rol, onClick = { viewModel.cambiarRol(rol) },
                            label = { Text(rol) })
                    }
                }
            }
        }
        estado.error?.let { error -> ancho { MensajeError(error, viewModel::cargar) } }
        if (estado.cargando) ancho { PieLista(cargando = true, hayMas = false) {} }
        if (!estado.cargando && estado.error == null && estado.visibles.isEmpty()) ancho {
            Text("Ningún agente coincide con el filtro.", modifier = Modifier.padding(vertical = 16.dp))
        }
        items(estado.visibles, key = { it.id }) { agente ->
            TarjetaImagen(
                imagen = agente.retrato ?: agente.icono, titulo = agente.nombre,
                subtitulo = agente.rol?.nombre ?: "Sin rol", fondo = degradado(agente),
                onClick = { seleccionado = agente }
            )
        }
    }

    seleccionado?.let { agente ->
        ModalBottomSheet(onDismissRequest = { seleccionado = null }) {
            DetalleAgente(agente)
        }
    }
}

@Composable
private fun DetalleAgente(agente: Agente) {
    Column(
        Modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp).padding(bottom = 32.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(24.dp)).background(degradado(agente))
        ) {
            AsyncImage(model = agente.fondo, contentDescription = null,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.35f)
            AsyncImage(model = agente.retrato ?: agente.icono, contentDescription = agente.nombre,
                modifier = Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Fit)
        }
        Spacer(Modifier.height(12.dp))
        Text(agente.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        agente.rol?.let { rol ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(model = rol.icono, contentDescription = null, modifier = Modifier.size(18.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(RojoValorant))
                Spacer(Modifier.width(6.dp))
                Text(rol.nombre, color = RojoValorant, fontWeight = FontWeight.SemiBold)
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(agente.descripcion, style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        Text("Habilidades", fontWeight = FontWeight.SemiBold)
        agente.habilidades.forEach { habilidad ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Box(
                    Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF0F1923)),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(model = habilidad.icono, contentDescription = null,
                        modifier = Modifier.size(30.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text("${habilidad.nombre} · ${habilidad.tecla}", fontWeight = FontWeight.SemiBold)
                    Text(habilidad.descripcion, style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
