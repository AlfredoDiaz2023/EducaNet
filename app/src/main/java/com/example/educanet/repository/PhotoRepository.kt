package com.example.educanet.repository

import android.content.Context
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.InputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine

/**
 * Repositorio encargado de la lógica de Firebase Storage (subir/descargar archivos).
 */
class PhotoRepository {

    private val storage = FirebaseStorage.getInstance()

    /**
     * Sube una imagen al Firebase Storage y retorna su URL pública.
     *
     * @param context Contexto de la aplicación.
     * @param imageUri La URI local de la imagen seleccionada.
     * @param path La ruta de destino en Firebase Storage (ej: "perfiles/uid/archivo.jpg").
     * @return La URL de descarga de la imagen subida.
     */
    suspend fun uploadImage(context: Context, imageUri: Uri, path: String): String {
        val storageRef = storage.getReference(path)

        // Usamos suspendCancellableCoroutine para manejar las tareas asíncronas de Firebase Storage
        return suspendCancellableCoroutine { continuation ->
            try {
                // Abrir el InputStream de la URI
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                    ?: throw Exception("No se pudo abrir la imagen.")

                // Subir el archivo
                val uploadTask = storageRef.putStream(inputStream!!)

                // 1. Esperar a que la subida esté completa
                uploadTask.addOnSuccessListener {
                    // 2. Obtener la URL de descarga
                    storageRef.downloadUrl
                        .addOnSuccessListener { uri ->
                            // 3. Devolver la URL
                            continuation.resume(uri.toString())
                        }
                        .addOnFailureListener { downloadException ->
                            continuation.resumeWithException(downloadException)
                        }
                }.addOnFailureListener { uploadException ->
                    continuation.resumeWithException(uploadException)
                }

                // Asegurar que la tarea se cancela si la corrutina se cancela
                continuation.invokeOnCancellation {
                    uploadTask.cancel()
                }
            } catch (e: Exception) {
                continuation.resumeWithException(e)
            }
        }
    }
}