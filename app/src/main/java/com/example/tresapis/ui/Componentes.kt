package com.example.tresapis.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

/** Elemento de la grilla que ocupa todo el ancho (encabezados, buscador, mensajes). */
fun LazyGridScope.ancho(contenido: @Composable () -> Unit) =
    item(span = { GridItemSpan(maxLineSpan) }) { contenido() }

@Composable
fun Encabezado(etiqueta: String, titulo: String, descripcion: String, acento: Color) {
    Column(Modifier.padding(top = 8.dp)) {
        Text(etiqueta, color = acento, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold)
        Text(titulo, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Text(descripcion, style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun CampoBusqueda(
    valor: String,
    onCambio: (String) -> Unit,
    onBuscar: () -> Unit,
    etiqueta: String,
    onLimpiar: () -> Unit = { onCambio("") }
) {
    val teclado = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = valor, onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth(), singleLine = true,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = if (valor.isNotEmpty()) {
            {
                IconButton(onClick = onLimpiar) {
                    Icon(Icons.Default.Clear, contentDescription = "Limpiar búsqueda")
                }
            }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { teclado?.hide(); onBuscar() }),
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun TarjetaImagen(
    imagen: String?,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit,
    fondo: Brush? = null,
    insignia: (@Composable () -> Unit)? = null,
    // null = la tarjeta no muestra corazón (Rick & Morty y Valorant).
    esFavorito: Boolean? = null,
    onFavoritoClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            Modifier.fillMaxWidth().aspectRatio(1f)
                .background(fondo ?: Brush.linearGradient(listOf(
                    MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant
                )))
        ) {
            // Sin fondo propio la imagen es una foto que llena la tarjeta; con fondo es un recorte que se centra.
            AsyncImage(
                model = imagen, contentDescription = titulo,
                modifier = Modifier.fillMaxSize().padding(if (fondo == null) 0.dp else 8.dp),
                contentScale = if (fondo == null) ContentScale.Crop else ContentScale.Fit
            )
            if (insignia != null) Box(Modifier.align(Alignment.TopStart).padding(8.dp)) { insignia() }
            if (esFavorito != null) BotonFavorito(esFavorito, onFavoritoClick, Modifier.align(Alignment.TopEnd))
        }
        Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
            Text(titulo, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(subtitulo, style = MaterialTheme.typography.bodySmall, maxLines = 1,
                overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

/** La tarjeta no sabe de Room: solo recibe si es favorito y avisa cuando se toca. */
@Composable
fun BotonFavorito(esFavorito: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    IconButton(onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = if (esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
            tint = if (esFavorito) Color(0xFFE53935) else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun Insignia(texto: String, color: Color) {
    Text(
        texto, color = Color.White, style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.clip(RoundedCornerShape(50)).background(color)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Composable
fun MensajeError(mensaje: String, onReintentar: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(mensaje, color = MaterialTheme.colorScheme.onErrorContainer, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onReintentar) { Text("Reintentar") }
        }
    }
}

@Composable
fun PieLista(cargando: Boolean, hayMas: Boolean, onCargarMas: () -> Unit) {
    Box(Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        when {
            cargando -> CircularProgressIndicator()
            hayMas -> FilledTonalButton(onClick = onCargarMas) { Text("Cargar más") }
        }
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(etiqueta, modifier = Modifier.width(110.dp), style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

fun String.capitalizar() = replaceFirstChar { it.uppercase() }
