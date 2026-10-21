package com.example.buscador.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.buscador.data.local.dao.UsuarioDao
import com.example.buscador.data.local.entity.UsuarioEntity

/**
 * Base de datos local de la aplicación.
 *
 * Decisiones técnicas:
 * - `exportSchema = false` porque aún no existen migraciones; cuando se incremente
 *   `version` habrá que exportar el esquema y escribir un Migration explícito.
 * - Patrón singleton con `@Volatile` + `synchronized` para garantizar una única
 *   instancia de conexión durante todo el ciclo de vida del proceso.
 * - El contexto siempre se toma como `applicationContext` para no retener Activities.
 */
@Database(
    entities = [UsuarioEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BuscadorDatabase : RoomDatabase() {

    /** DAO expuesto a la capa repositorio; nunca se usa directamente desde la UI. */
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        private const val NOMBRE_BASE_DE_DATOS = "buscador_cenamad.db"

        @Volatile
        private var instancia: BuscadorDatabase? = null

        /** Devuelve (y crea si hace falta) la instancia única de la base de datos. */
        fun obtenerInstancia(contexto: Context): BuscadorDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    contexto.applicationContext,
                    BuscadorDatabase::class.java,
                    NOMBRE_BASE_DE_DATOS
                ).build().also { baseDeDatosCreada ->
                    instancia = baseDeDatosCreada
                }
            }
        }
    }
}
