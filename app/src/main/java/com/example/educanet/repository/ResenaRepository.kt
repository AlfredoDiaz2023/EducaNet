package com.example.educanet.repository

import com.example.educanet.model.Resena
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ResenaRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarResena(resena: Resena): Boolean {
        return try {
            // Crear un mapa sin el campo id para evitar guardar id vacío
            val resenaData = hashMapOf(
                "itemId" to resena.itemId,
                "userId" to resena.userId,
                "userName" to resena.userName,
                "rating" to resena.rating,
                "comment" to resena.comment,
                "timestamp" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )
            db.collection("resenas").add(resenaData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerResenas(itemId: String): List<Resena> {
        return try {
            val snapshot = db.collection("resenas")
                .whereEqualTo("itemId", itemId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            // Corregir la conversión de documentos y asignar el ID
            snapshot.documents.mapNotNull {
                it.toObject(Resena::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // Obtener todas las reseñas recientes con información del libro
    suspend fun obtenerResenasRecientes(limite: Int = 20): List<ResenaConLibro> {
        return try {
            val resenasSnapshot = db.collection("resenas")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limite.toLong())
                .get()
                .await()

            val resenas = resenasSnapshot.documents.mapNotNull { doc ->
                val resena = doc.toObject(Resena::class.java)?.copy(id = doc.id)
                resena
            }

            // Obtener nombres de libros para cada reseña
            resenas.map { resena ->
                var libroNombre = "Libro desconocido"
                try {
                    val libroDoc = db.collection("libro").document(resena.itemId).get().await()
                    libroNombre = libroDoc.getString("nombre") ?: "Libro desconocido"
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                ResenaConLibro(resena, libroNombre)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

data class ResenaConLibro(
    val resena: Resena,
    val libroNombre: String
)
