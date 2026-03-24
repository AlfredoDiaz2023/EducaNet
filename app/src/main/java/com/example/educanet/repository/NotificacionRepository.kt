package com.example.educanet.repository

import com.example.educanet.model.Notificacion
import com.example.educanet.model.TipoNotificacion
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

    // Obtener notificaciones para un usuario específico (solo las suyas + las generales "todos" + las de su curso)
    suspend fun obtenerNotificacionesPorUsuario(userId: String, curso: String = ""): List<Notificacion> {
        return try {
            val snapshot = db.collection("notificaciones")
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            android.util.Log.d("NotificacionRepo", "Total notificaciones en DB: ${snapshot.documents.size}")
            android.util.Log.d("NotificacionRepo", "Buscando para userId: $userId, curso: '$curso'")
            
            val resultado = snapshot.documents.mapNotNull { doc ->
                val notif = doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
                
                if (notif != null) {
                    val cursoNotif = notif.curso.trim()
                    val cursoUsuario = curso.trim()
                    
                    // Filtrar: mostrar si es para este usuario específico, para todos, o para su curso
                    val esParaUsuario = notif.userId == userId
                    val esParaTodos = notif.tipoDestinatario == "todos"
                    val esParaCurso = notif.tipoDestinatario == "curso" && 
                                      cursoNotif.equals(cursoUsuario, ignoreCase = true)
                    
                    if (esParaUsuario || esParaTodos || esParaCurso) {
                        android.util.Log.d("NotificacionRepo", "✓ Notificación incluida: ${notif.titulo} (tipo: ${notif.tipoDestinatario}, curso: '$cursoNotif')")
                        notif
                    } else {
                        null
                    }
                } else null
            }
            
            android.util.Log.d("NotificacionRepo", "Notificaciones filtradas: ${resultado.size}")
            resultado
        } catch (e: Exception) {
            android.util.Log.e("NotificacionRepo", "Error obteniendo notificaciones", e)
            emptyList()
        }
    }

    // Obtener notificaciones para administrador (ve TODAS las notificaciones de todos los usuarios)
    suspend fun obtenerNotificacionesParaAdmin(): List<Notificacion> {
        return try {
            val snapshot = db.collection("notificaciones")
                .orderBy("fecha", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            // Admin ve absolutamente todas las notificaciones (alumnos, apoderados, profesores, etc.)
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

    // Verificar si hay notificaciones sin leer para un usuario específico (incluyendo su curso)
    suspend fun hayNotificacionesSinLeerParaUsuario(userId: String, esAdmin: Boolean, curso: String = ""): Boolean {
        return try {
            val snapshot = db.collection("notificaciones")
                .whereEqualTo("isRead", false)
                .get()
                .await()
            
            snapshot.documents.any { doc ->
                val tipoDestinatario = doc.getString("tipoDestinatario") ?: "todos"
                val notifUserId = doc.getString("userId") ?: ""
                val notifCurso = doc.getString("curso") ?: ""
                
                when {
                    esAdmin && (tipoDestinatario == "admin" || tipoDestinatario == "todos") -> true
                    notifUserId == userId -> true
                    tipoDestinatario == "todos" -> true
                    // Verificar si es una notificación de curso y coincide con el curso del usuario
                    tipoDestinatario == "curso" && curso.isNotEmpty() && 
                        notifCurso.equals(curso, ignoreCase = true) -> true
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

    /**
     * Notifica a todos los alumnos de un curso que se ha iniciado una clase
     */
    suspend fun notificarInicioClase(
        claseId: String,
        asignatura: String,
        curso: String,
        profesorNombre: String
    ): Boolean {
        return try {
            android.util.Log.d("NotificacionRepo", "📢 Enviando notificación de clase iniciada")
            android.util.Log.d("NotificacionRepo", "   claseId: $claseId")
            android.util.Log.d("NotificacionRepo", "   asignatura: $asignatura")
            android.util.Log.d("NotificacionRepo", "   curso: '$curso'")
            android.util.Log.d("NotificacionRepo", "   profesor: $profesorNombre")
            
            val notificacion = hashMapOf(
                "titulo" to "📚 ¡Clase Iniciada!",
                "mensaje" to "El profesor $profesorNombre ha iniciado la clase de $asignatura. ¡Únete ahora para registrar tu asistencia!",
                "fecha" to System.currentTimeMillis(),
                "isRead" to false,
                "userId" to "",
                "tipoDestinatario" to "curso",
                "tipoNotificacion" to TipoNotificacion.CLASE_INICIADA.name,
                "claseId" to claseId,
                "asignatura" to asignatura,
                "curso" to curso.trim(), // Asegurar sin espacios extra
                "profesorNombre" to profesorNombre,
                "accionable" to true,
                "accionRealizada" to false
            )
            
            val docRef = db.collection("notificaciones").add(notificacion).await()
            android.util.Log.d("NotificacionRepo", "✅ Notificación creada con ID: ${docRef.id}")
            true
        } catch (e: Exception) {
            android.util.Log.e("NotificacionRepo", "❌ Error creando notificación de clase", e)
            false
        }
    }

    /**
     * Obtener notificaciones de clases activas para un curso específico
     */
    suspend fun obtenerNotificacionesClaseActiva(curso: String): List<Notificacion> {
        return try {
            val snapshot = db.collection("notificaciones")
                .whereEqualTo("tipoDestinatario", "curso")
                .whereEqualTo("curso", curso)
                .whereEqualTo("tipoNotificacion", TipoNotificacion.CLASE_INICIADA.name)
                .whereEqualTo("accionable", true)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Notificacion::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.fecha }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Marcar que el alumno se unió a la clase (para su notificación específica)
     */
    suspend fun marcarUnionAClase(notificacionId: String) {
        try {
            db.collection("notificaciones")
                .document(notificacionId)
                .update(mapOf(
                    "accionRealizada" to true,
                    "isRead" to true
                ))
                .await()
        } catch (e: Exception) {
            // Manejar error
        }
    }

    /**
     * Desactivar notificaciones de una clase (cuando el profesor finaliza)
     */
    suspend fun desactivarNotificacionesClase(claseId: String) {
        try {
            val snapshot = db.collection("notificaciones")
                .whereEqualTo("claseId", claseId)
                .whereEqualTo("tipoNotificacion", TipoNotificacion.CLASE_INICIADA.name)
                .get()
                .await()
            
            val batch = db.batch()
            for (doc in snapshot.documents) {
                batch.update(doc.reference, "accionable", false)
            }
            batch.commit().await()
        } catch (e: Exception) {
            // Manejar error
        }
    }
}