package com.example.tresapis.viewmodel

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tresapis.modelo.Usuario
import com.example.tresapis.repository.AuthRepository
import com.example.tresapis.repository.Resultado
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Estado del formulario de inicio de sesión y registro. */
data class AuthUiState(
    val email: String = "",
    val contrasena: String = "",
    val modoRegistro: Boolean = false,
    val mostrarContrasena: Boolean = false,
    val cargando: Boolean = false,
    val error: String? = null,
    val info: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {
    // Dos estados distintos: el formulario (lo que se escribe) y la sesión (quién está dentro).
    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    // El valor inicial es usuarioActual(): si ya había sesión, la app abre directo en las pestañas.
    val usuario: StateFlow<Usuario?> = repository.observarSesion()
        .stateIn(viewModelScope, SharingStarted.Eagerly, repository.usuarioActual())

    fun cambiarEmail(valor: String) = _estado.update { it.copy(email = valor, error = null, info = null) }

    fun cambiarContrasena(valor: String) = _estado.update { it.copy(contrasena = valor, error = null, info = null) }

    fun alternarMostrarContrasena() = _estado.update { it.copy(mostrarContrasena = !it.mostrarContrasena) }

    fun alternarModo() = _estado.update { it.copy(modoRegistro = !it.modoRegistro, error = null, info = null) }

    fun enviar() {
        val actual = _estado.value
        val email = actual.email.trim()
        validar(email, actual.contrasena)?.let { mensaje ->
            _estado.update { it.copy(error = mensaje) }
            return
        }
        _estado.update { it.copy(cargando = true, error = null, info = null) }
        viewModelScope.launch {
            val resultado = if (actual.modoRegistro) repository.registrar(email, actual.contrasena)
            else repository.iniciarSesion(email, actual.contrasena)
            // Si sale bien no se navega desde aquí: `usuario` cambia y TresApisApp muestra las pestañas.
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(cargando = false, contrasena = "")
                    is Resultado.Error -> it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun recuperarContrasena() {
        val email = _estado.value.email.trim()
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            _estado.update { it.copy(error = "Escribe tu correo para enviarte el enlace de recuperación.") }
            return
        }
        _estado.update { it.copy(cargando = true, error = null, info = null) }
        viewModelScope.launch {
            val resultado = repository.enviarCorreoDeRecuperacion(email)
            _estado.update {
                when (resultado) {
                    is Resultado.Exito -> it.copy(cargando = false, info = "Te enviamos un correo para restablecer la contraseña.")
                    is Resultado.Error -> it.copy(cargando = false, error = resultado.mensaje)
                }
            }
        }
    }

    fun cerrarSesion() {
        repository.cerrarSesion()
        _estado.value = AuthUiState()
    }

    /** Validación en el teléfono: ahorra una llamada a Firebase cuando el dato es obviamente malo. */
    private fun validar(email: String, contrasena: String): String? = when {
        !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> "Escribe un correo válido."
        contrasena.length < 6 -> "La contraseña debe tener al menos 6 caracteres."
        else -> null
    }
}
