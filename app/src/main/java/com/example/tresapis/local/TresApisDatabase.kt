package com.example.tresapis.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Database = reúne las tablas (entities) y entrega los DAO.
@Database(
    entities = [FavoritoEntity::class, PokemonCacheEntity::class], // se agregó la tabla de la caché
    version = 2,                                                    // era 1
    exportSchema = false
)
abstract class TresApisDatabase : RoomDatabase() {
    abstract fun favoritoDao(): FavoritoDao
    abstract fun pokemonCacheDao(): PokemonCacheDao

    companion object {
        @Volatile
        private var instancia: TresApisDatabase? = null

        // Pasa de la versión 1 a la 2 sin perder los favoritos: solo crea la tabla nueva.
        // El SQL debe describir la tabla exactamente igual que PokemonCacheEntity.
        private val MIGRACION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pokemon_cache` (" +
                        "`id` INTEGER NOT NULL, " +
                        "`nombre` TEXT NOT NULL, " +
                        "`imagen` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
            }
        }

        // Singleton: una sola instancia de la base de datos en toda la app.
        fun obtener(context: Context): TresApisDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    TresApisDatabase::class.java,
                    "tresapis.db"
                )
                    .addMigrations(MIGRACION_1_2)
                    .build()
                    .also { instancia = it }
            }
    }
}
