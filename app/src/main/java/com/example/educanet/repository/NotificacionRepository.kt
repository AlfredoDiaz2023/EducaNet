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

    // Obtener notificaciones para un usuario específico (las suyas + las de admin/todos)
    suspend fun obtenerNotificacionesPorUsuario(userId: String): List<Notificacion> {
        return try {
            val snapshot = db.collection("notificaciones")
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val notif = doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
                // Filtrar: mostrar si es para este usuario, para admin, o para todos
                if (notif != null && (notif.userId == userId || notif.tipoDestinatario == "todos" || notif.tipoDestinatario == "admin")) {
                    notif
                } else null
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
            "isRead" to false,
            "userId" to "",
            "tipoDestinatario" to "todos"
        )
        db.collection("notificaciones").add(nueva).await()
    }

    // Notificación para un usuario específico
    suspend fun agregarNotificacionParaUsuario(titulo: String, mensaje: String, userId: String) {
        val nueva = hashMapOf(
            "titulo" to titulo,
            "mensaje" to mensaje,
            "fecha" to System.currentTimeMillis(),
            "isRead" to false,
            "userId" to userId,
            "tipoDestinatario" to "usuario"
        )
        db.collection("notificaciones").add(nueva).await()
    }

    // Notificación solo para admin
    suspend fun agregarNotificacionParaAdmin(titulo: String, mensaje: String) {
        val nueva = hashMapOf(
            "titulo" to titulo,
            "mensaje" to mensaje,
            "fecha" to System.currentTimeMillis(),
            "isRead" to false,
            "userId" to "",
            "tipoDestinatario" to "admin"
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

    // Verificar si hay notificaciones sin leer para un usuario específico
    suspend fun hayNotificacionesSinLeerParaUsuario(userId: String, esAdmin: Boolean): Boolean {
        return try {
            val snapshot = db.collection("notificaciones")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            snapshot.documents.any { doc ->
                val tipoDestinatario = doc.getString("tipoDestinatario") ?: "todos"
                val notifUserId = doc.getString("userId") ?: ""
                
                when {
                    esAdmin && (tipoDestinatario == "admin" || tipoDestinatario == "todos") -> true
                    notifUserId == userId -> true
                    tipoDestinatario == "todos" -> true
                    else -> false
                }
            }
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