package com.example.educanet.model

/**
 * Tipos de notificación
 */
enum class TipoNotificacion(val displayName: String) {
    GENERAL("General"),
    CLASE_INICIADA("Clase Iniciada"),
    TAREA("Tarea"),
    CALIFICACION("Calificación"),
    ASISTENCIA("Asistencia"),
    MENSAJE("Mensaje")
}

data class Notificacion(
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val userId: String = "",  // ID del usuario destinatario (vacío = para todos/admin)
    val tipoDestinatario: String = "todos",  // "todos", "usuario", "admin", "curso"
    val tipoNotificacion: String = TipoNotificacion.GENERAL.name,
    // Campos para notificaciones de clase
    val claseId: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val profesorNombre: String = "",
    // Campo para acción (para unirse a clase, etc.)
    val accionable: Boolean = false,
    val accionRealizada: Boolean = false
)