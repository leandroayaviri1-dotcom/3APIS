package com.example.tresapis.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Color de acento de cada sección
val VerdePortal = Color(0xFF3FA34D)
val RojoPokebola = Color(0xFFE3350D)
val RojoValorant = Color(0xFFFF4655)

private val claro = lightColorScheme(
    primary = Color(0xFF3D5AFE), onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF), onPrimaryContainer = Color(0xFF001257),
    background = Color(0xFFF4F5FA), surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE7E8F0)
)
private val oscuro = darkColorScheme(
    primary = Color(0xFFB9C3FF), primaryContainer = Color(0xFF2A3A8F),
    background = Color(0xFF111318), surface = Color(0xFF1B1D24),
    surfaceVariant = Color(0xFF2A2D36)
)

@Composable
fun TresApisTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) oscuro else claro, content = content)
}
