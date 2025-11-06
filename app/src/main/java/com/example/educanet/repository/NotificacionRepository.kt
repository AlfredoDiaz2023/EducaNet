package com.example.educanet.repository

import com.example.educanet.model.Notificacion
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificacionRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerNotificaciones(): List<Notificacion> {
        return try {
            val snapshot = db.collection("notificaciones")
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun agregarNotificacion(titulo: String, mensaje: String) {
        val nueva = hashMapOf(
            "titulo" to titulo,
            "mensaje" to mensaje,
            "fecha" to System.currentTimeMillis(),
            "isRead" to false
        )
        db.collection("notificaciones").add(nueva).await()
    }

    suspend fun hayNotificacionesSinLeer(): Boolean {
        return try {
            val snapshot = db.collection("notificaciones")
                .whereEqualTo("isRead", false)
                .limit(1)
                .get()
                .await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    suspend fun marcarTodasComoLeidas() {
        try {
            val snapshot = db.collection("notificaciones")
                .whereEqualTo("isRead", false)
                .get()
                .await()

            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "isRead", true)
            }
            batch.commit().await()
        } catch (e: Exception) {
            // Manejar la excepción
        }
    }

    suspend fun eliminarNotificacion(id: String) {
        db.collection("notificaciones").document(id).delete().await()
    }
}