package com.example.buscador.repository

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.buscador.model.InvestigadorEntity
import com.example.buscador.model.LineaInvestigacionEntity
import com.example.buscador.model.UsuarioEntity

/**
 * Base de datos local de la aplicación.
 *
 * Decisiones técnicas:
 * - `exportSchema = false` porque el proyecto aún no conserva el esquema en JSON;
 *   los cambios de tabla se resuelven destruyendo y recreando la base.
 * - `fallbackToDestructiveMigration(dropAllTables = true)`: los cambios de esquema
 *   (v2: columna `contrasena`; v3: tablas del caso CENAMAD) se resuelven destruyendo
 *   y recreando la base, porque solo hay datos locales de desarrollo; evita el crash
 *   por IllegalStateException ante cada incremento de versión.
 * - Patrón singleton con `@Volatile` + `synchronized` para garantizar una única
 *   instancia de conexión durante todo el ciclo de vida del proceso.
 * - El contexto siempre se toma como `applicationContext` para no retener Activities.
 */
@Database(
    entities = [
        UsuarioEntity::class,
        LineaInvestigacionEntity::class,
        InvestigadorEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BuscadorDatabase : RoomDatabase() {

    /** DAO expuesto a la capa repositorio; nunca se usa directamente desde la UI. */
    abstract fun usuarioDao(): UsuarioDao

    /** DAO del caso CENAMAD (líneas de investigación e investigadores). */
    abstract fun cenamadDao(): CenamadDao

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
                ).fallbackToDestructiveMigration(dropAllTables = true).build().also { baseDeDatosCreada ->
                    instancia = baseDeDatosCreada
                }
            }
        }
    }
}
