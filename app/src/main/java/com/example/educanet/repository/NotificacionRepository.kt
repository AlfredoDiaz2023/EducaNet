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
            "fecha" to System.currentTimeMillis()
        )
        db.collection("notificaciones").add(nueva).await()
    }

    suspend fun eliminarNotificacion(id: String) {
        db.collection("notificaciones").document(id).delete().await()
    }
}