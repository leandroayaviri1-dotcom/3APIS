package com.example.tresapis.repository

/** Respuesta de una operación de AuthRepository: salió bien, o falló con un mensaje para mostrar. */
sealed class Resultado<out T> {
    data class Exito<T>(val valor: T) : Resultado<T>()
    data class Error(val mensaje: String) : Resultado<Nothing>()
}
