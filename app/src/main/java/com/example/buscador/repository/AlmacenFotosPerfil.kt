package com.example.buscador.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.coroutines.cancellation.CancellationException

/**
 * Almacén de las fotos de perfil dentro del espacio privado de la aplicación.
 *
 * Decisiones técnicas:
 * - Las imágenes viven en `filesDir/fotos_perfil`, que el sistema no limpia de forma
 *   automática (a diferencia de `cacheDir`) y no exige permisos de almacenamiento.
 * - A Room solo se envía la URI de esa copia, obtenida con [FileProvider]. Así la foto
 *   sigue siendo legible tras reiniciar el proceso: las URIs del Photo Picker son
 *   temporales y expiran, lo que dejaría el perfil con una imagen rota.
 * - La captura de cámara escribe directo en esta carpeta mediante [crearUriParaCamara],
 *   por lo que no se duplica el archivo al volver del intent.
 *
 * No conoce Compose ni Room: solo entrega URIs listas para persistir.
 */
class AlmacenFotosPerfil(contexto: Context) {

    /**
     * Se guarda el contexto de aplicación para no retener Activities.
     * `ContentResolver` y `FileProvider` funcionan igual con este contexto.
     */
    private val contextoAplicacion: Context = contexto.applicationContext

    /** Autoridad declarada en AndroidManifest.xml (`${applicationId}.fileprovider`). */
    private val autoridadProveedor: String =
        "${contextoAplicacion.packageName}$SUFIJO_AUTORIDAD"

    /**
     * Crea la URI de destino de una nueva captura de cámara.
     *
     * El archivo queda creado (vacío) en el almacenamiento propio y la app de cámara
     * lo sobrescribe al confirmar la foto. Se crea de antemano para que el proveedor
     * pueda responder consultas de metadatos (nombre, tamaño) antes de escribir.
     * Si el usuario cancela, el llamador debe liberarlo con [eliminarFotoSiEsPropia].
     *
     * @throws IllegalArgumentException si FileProvider no tiene la ruta configurada.
     * @throws java.io.IOException si no se puede crear el archivo destino.
     */
    fun crearUriParaCamara(): Uri {
        val archivoDestino = File(
            obtenerDirectorioFotos(),
            "camara_${System.currentTimeMillis()}$EXTENSION_PREDETERMINADA"
        )
        archivoDestino.createNewFile()
        return FileProvider.getUriForFile(
            contextoAplicacion,
            autoridadProveedor,
            archivoDestino
        )
    }

    /**
     * Copia una imagen elegida en la galería al almacenamiento propio.
     *
     * La copia ocurre en [Dispatchers.IO] porque leer el `content://` del selector
     * implica I/O. Si la lectura falla, el archivo parcial se elimina para no dejar
     * basura y se devuelve null.
     *
     * @param uriOrigen URI temporal entregada por el Photo Picker.
     * @return URI estable de la copia, o null si la imagen no se pudo leer.
     */
    suspend fun copiarDesdeGaleria(uriOrigen: Uri): Uri? = withContext(Dispatchers.IO) {
        val archivoDestino = File(
            obtenerDirectorioFotos(),
            "galeria_${System.currentTimeMillis()}${extensionDe(uriOrigen)}"
        )

        try {
            val flujoEntrada = contextoAplicacion.contentResolver
                .openInputStream(uriOrigen)
                ?: return@withContext null

            flujoEntrada.use { entrada ->
                archivoDestino.outputStream().use { salida ->
                    entrada.copyTo(salida)
                }
            }

            FileProvider.getUriForFile(
                contextoAplicacion,
                autoridadProveedor,
                archivoDestino
            )
        } catch (cancelacion: CancellationException) {
            // La pantalla se cerró mientras se copiaba: no es un error de negocio.
            archivoDestino.delete()
            throw cancelacion
        } catch (error: Exception) {
            archivoDestino.delete()
            Log.w(ETIQUETA_LOG, "No se pudo copiar la imagen de la galería.", error)
            null
        }
    }

    /**
     * Elimina una foto creada por esta clase (captura cancelada o imagen reemplazada).
     *
     * Solo actúa si la URI pertenece a este FileProvider: una URI antigua del Photo
     * Picker apunta a un archivo ajeno que la app no debe borrar.
     */
    fun eliminarFotoSiEsPropia(uriTexto: String?) {
        val uri = uriTexto?.takeIf { texto -> texto.isNotBlank() }?.let(Uri::parse) ?: return
        if (uri.authority != autoridadProveedor) return

        val nombreArchivo = uri.lastPathSegment ?: return
        val archivo = File(obtenerDirectorioFotos(), nombreArchivo)
        if (archivo.exists() && !archivo.delete()) {
            Log.w(ETIQUETA_LOG, "No se pudo eliminar la foto: $nombreArchivo")
        }
    }

    /** Devuelve (y crea si falta) la carpeta privada donde se guardan las fotos. */
    private fun obtenerDirectorioFotos(): File =
        File(contextoAplicacion.filesDir, CARPETA_FOTOS).apply { mkdirs() }

    /**
     * Deduce la extensión del archivo copiado a partir del MIME del origen.
     * El decodificador de Coil detecta el formato por contenido, así que la
     * extensión es informativa; se usa `.jpg` cuando el tipo no es reconocible.
     */
    private fun extensionDe(uriOrigen: Uri): String {
        val tipoContenido = contextoAplicacion.contentResolver.getType(uriOrigen)
        val extension = tipoContenido
            ?.let { tipo -> MimeTypeMap.getSingleton().getExtensionFromMimeType(tipo) }

        return if (extension.isNullOrBlank()) EXTENSION_PREDETERMINADA else ".$extension"
    }

    companion object {
        /** Subcarpeta de `filesDir`; debe coincidir con res/xml/file_paths.xml. */
        private const val CARPETA_FOTOS = "fotos_perfil"

        /** Sufijo de la autoridad de FileProvider declarada en el manifiesto. */
        private const val SUFIJO_AUTORIDAD = ".fileprovider"

        /** TakePicture siempre entrega JPEG. */
        private const val EXTENSION_PREDETERMINADA = ".jpg"

        /** Etiqueta de Log para diagnosticar fallos de lectura o escritura. */
        private const val ETIQUETA_LOG = "AlmacenFotosPerfil"
    }
}
