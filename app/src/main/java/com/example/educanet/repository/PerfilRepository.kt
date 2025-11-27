package com.example.educanet.repository


import android.content.Context
import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.*

class PerfilRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // 1. Función para subir la imagen a Firebase Storage y obtener la URL
    suspend fun subirImagenPerfil(context: Context, uri: Uri, userId: String): String {
        return try {
            // Creamos una referencia única en Firebase Storage
            val storageRef = storage.reference.child("perfiles/$userId/${UUID.randomUUID()}.jpg")

            // Subir el archivo
            storageRef.putFile(uri).await()

            // Obtener la URL de descarga para guardarla en Firestore
            val url = storageRef.downloadUrl.await().toString()
            url
        } catch (e: Exception) {
            e.printStackTrace()
            // Lanzamos una excepción para que el ViewModel pueda manejar el error
            throw Exception("Error al subir la imagen a Storage: ${e.message}")
        }
    }

    // 2. Función para actualizar la URL de la foto de perfil en Firestore
    // docId es el ID del documento en la colección (ej. 'Alumno' o 'Apoderado')
    suspend fun actualizarFotoUrl(rol: String, docId: String, nuevaFotoUrl: String): Boolean {
        return try {
            // Determinamos la colección según el rol
            val collectionName = when (rol) {
                "Alumno" -> "Alumno"
                "Apoderado" -> "Apoderado"
                "Profesor" -> "Profesor" // Asegúrate de que tienes una colección para Profesor
                "Administrador" -> "Administrador" // Asegúrate de que tienes una colección para Administrador
                else -> throw IllegalArgumentException("Rol de usuario desconocido")
            }

            db.collection(collectionName)
                .document(docId)
                .update("fotoUrl", nuevaFotoUrl) // ¡Asegúrate de que tus documentos tienen un campo "fotoUrl"!
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}