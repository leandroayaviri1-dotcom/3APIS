package com.example.tresapis

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory

// Coil guarda en disco las imágenes que descarga, pero las de GitHub llegan marcadas como
// "vencidas" a los 5 minutos. Así se reutiliza la copia guardada aunque esté vencida.
class TresApisApplication : Application(), ImageLoaderFactory {
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .respectCacheHeaders(false)
            .build()
}
