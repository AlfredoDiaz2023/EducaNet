package com.example.educanet.repository

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class PhotoRepository {
    // Usa el bucket por defecto del proyecto configurado por google-services.json
    private val storage = FirebaseStorage.getInstance()

    suspend fun subirFoto(uri: Uri, nombreArchivo: String): String {
        Log.d("EDUCA_DEBUG", "PhotoRepository: Iniciando subida de: $uri")
        Log.d("EDUCA_DEBUG", "PhotoRepository: Nombre archivo: $nombreArchivo")
        
        val pathCompleto = "usuario/$nombreArchivo"
        val ref = storage.reference.child(pathCompleto)

        return try {
            // 1. Subir el archivo
            Log.d("EDUCA_DEBUG", "PhotoRepository: Subiendo archivo...")
            val uploadTask = ref.putFile(uri).await()
            Log.d("EDUCA_DEBUG", "PhotoRepository: Archivo subido exitosamente")
            
            // 2. CRÍTICO: Obtener URL pública de descarga (https://)
            val url = ref.downloadUrl.await().toString()
            Log.d("EDUCA_DEBUG", "PhotoRepository: URL HTTPS generada -> $url")
            
            // 3. Verificar que la URL sea válida
            if (!url.startsWith("https://")) {
                throw Exception("URL inválida generada: $url")
            }
            
            url
        } catch (e: Exception) {
            Log.e("EDUCA_DEBUG", "PhotoRepository ERROR: ${e.message}", e)
            throw Exception("Error al subir foto: ${e.message}", e)
        }
    }
}