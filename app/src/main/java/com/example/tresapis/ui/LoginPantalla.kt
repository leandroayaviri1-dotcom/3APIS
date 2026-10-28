package com.example.tresapis.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.tresapis.viewmodel.AuthViewModel

// Una sola pantalla para entrar y para crear cuenta: un botón de texto cambia el modo.
// Solo dibuja el estado y avisa de los eventos; no valida ni llama a Firebase.
@Composable
fun LoginPantalla(viewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(if (estado.modoRegistro) "Crear cuenta" else "Iniciar sesión",
            style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Tres APIs", style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = estado.email,
            onValueChange = viewModel::cambiarEmail,
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = estado.contrasena,
            onValueChange = viewModel::cambiarContrasena,
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (estado.mostrarContrasena) VisualTransformation.None
                else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = viewModel::alternarMostrarContrasena) {
                    Text(if (estado.mostrarContrasena) "Ocultar" else "Mostrar")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        estado.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp))
        }
        estado.info?.let {
            Text(it, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 12.dp))
        }

        // Mientras carga, los botones se desactivan: así no se envía dos veces.
        Button(
            onClick = viewModel::enviar,
            enabled = !estado.cargando,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(48.dp)
        ) {
            if (estado.cargando) CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            else Text(if (estado.modoRegistro) "Crear cuenta" else "Entrar")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = viewModel::alternarModo, enabled = !estado.cargando) {
                Text(if (estado.modoRegistro) "Ya tengo cuenta" else "Crear una cuenta")
            }
            if (!estado.modoRegistro) {
                TextButton(onClick = viewModel::recuperarContrasena, enabled = !estado.cargando) {
                    Text("Olvidé mi contraseña")
                }
            }
        }
    }
}
