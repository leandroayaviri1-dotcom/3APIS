package com.example.tresapis.repository

import com.example.tresapis.modelo.Usuario
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// La única clase que conoce a FirebaseAuth: el ViewModel y la pantalla solo ven Usuario y Resultado.
class AuthRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    /** Usuario con la sesión abierta ahora mismo, o null. Firebase la recuerda sin pedir nada a Internet. */
    fun usuarioActual(): Usuario? = auth.currentUser?.aUsuario()

    /** Emite el usuario cada vez que se inicia o se cierra la sesión (null = sin sesión). */
    fun observarSesion(): Flow<Usuario?> = callbackFlow {
        val oyente = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.aUsuario()) }
        auth.addAuthStateListener(oyente)
        awaitClose { auth.removeAuthStateListener(oyente) }
    }

    suspend fun iniciarSesion(email: String, contrasena: String): Resultado<Unit> =
        llamar { auth.signInWithEmailAndPassword(email, contrasena).await() }

    suspend fun registrar(email: String, contrasena: String): Resultado<Unit> =
        llamar { auth.createUserWithEmailAndPassword(email, contrasena).await() }

    suspend fun enviarCorreoDeRecuperacion(email: String): Resultado<Unit> =
        llamar { auth.sendPasswordResetEmail(email).await() }

    fun cerrarSesion() = auth.signOut()

    private fun FirebaseUser.aUsuario() = Usuario(uid = uid, email = email.orEmpty())

    /** Ejecuta la llamada y traduce cualquier excepción de Firebase a un [Resultado.Error]. */
    private suspend fun llamar(bloque: suspend () -> Unit): Resultado<Unit> =
        try {
            bloque()
            Resultado.Exito(Unit)
        } catch (e: CancellationException) {
            throw e // la cancelación no es un error: debe propagarse
        } catch (e: Exception) {
            Resultado.Error(traducir(e))
        }

    // Correo inexistente y contraseña incorrecta dan el mismo mensaje: no se revela qué correos existen.
    private fun traducir(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "La contraseña es muy débil. Usa al menos 6 caracteres."
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese correo."
        is FirebaseAuthInvalidUserException -> "No existe una cuenta con ese correo."
        is FirebaseAuthInvalidCredentialsException -> "Correo o contraseña incorrectos."
        is FirebaseNetworkException -> "No hay conexión a Internet. Revisa tu red e inténtalo de nuevo."
        else -> "No se pudo completar la operación. Inténtalo de nuevo."
    }
}
