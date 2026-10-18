package com.example.tresapis.repository

import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class ApiException(message: String) : Exception(message)

/** Ejecuta una llamada y traduce los errores técnicos a mensajes para el usuario. */
internal suspend fun <T> llamar(servicio: String, siNoExiste: String, bloque: suspend () -> T): T {
    try {
        return bloque()
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        throw ApiException(
            if (e.code() == 404) siNoExiste
            else "$servicio no pudo responder (HTTP ${e.code()}). Intenta nuevamente."
        )
    } catch (e: SocketTimeoutException) {
        throw ApiException("$servicio tardó demasiado en responder. Intenta nuevamente.")
    } catch (e: IOException) {
        throw ApiException("No se pudo conectar. Comprueba tu conexión a Internet.")
    } catch (e: JsonParseException) {
        throw ApiException("$servicio devolvió datos que no se pudieron interpretar.")
    }
}
