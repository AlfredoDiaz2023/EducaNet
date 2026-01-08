package com.example.educanet.model

/**
 * Representa una clase en curso con toda su información
 */
data class ClaseEnCurso(
    val id: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val horaInicio: Long = System.currentTimeMillis(),
    val horaFin: Long? = null,
    val activa: Boolean = true,
    val fecha: Long = System.currentTimeMillis(),
    val duracionMinutos: Int = HorarioConstantes.DURACION_CLASE_MINUTOS
)

/**
 * Representa la asistencia de un alumno en una clase específica
 */
data class AsistenciaClase(
    val id: String = "",
    val claseId: String = "",
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val presente: Boolean = true,
    val horaRegistro: Long = System.currentTimeMillis(),
    val justificacion: String = ""
)

/**
 * Representa una calificación rápida durante la clase
 */
data class CalificacionClase(
    val id: String = "",
    val claseId: String = "",
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val nota: Double = 0.0,
    val tipo: TipoCalificacion = TipoCalificacion.PARTICIPACION,
    val descripcion: String = "",
    val fecha: Long = System.currentTimeMillis()
)

/**
 * Tipos de calificación durante la clase
 */
enum class TipoCalificacion(val displayName: String) {
    PARTICIPACION("Participación"),
    TAREA("Tarea"),
    QUIZ("Quiz"),
    TRABAJO_EN_CLASE("Trabajo en Clase"),
    EXPOSICION("Exposición"),
    OTRO("Otro")
}

/**
 * Representa un material de clase
 */
data class MaterialClase(
    val id: String = "",
    val claseId: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val titulo: String = "",
    val descripcion: String = "",
    val tipo: TipoMaterial = TipoMaterial.DOCUMENTO,
    val url: String = "",
    val nombreArchivo: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val fechaSubida: Long = System.currentTimeMillis(),
    val visible: Boolean = true
)

/**
 * Tipos de material
 */
enum class TipoMaterial(val displayName: String, val extension: String) {
    DOCUMENTO("Documento", "pdf,doc,docx"),
    IMAGEN("Imagen", "jpg,png,gif"),
    VIDEO("Video", "mp4,avi,mov"),
    PRESENTACION("Presentación", "ppt,pptx"),
    ENLACE("Enlace", ""),
    OTRO("Otro", "*")
}

/**
 * Estado de un alumno durante la clase
 */
data class AlumnoEnClase(
    val alumno: Alumno,
    val presente: Boolean = true,
    val notaClase: Double? = null,
    val participaciones: Int = 0
)

/**
 * Representa un alumno conectado a una clase en tiempo real
 */
data class AlumnoConectado(
    val id: String = "",
    val claseId: String = "",
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val alumnoCorreo: String = "",
    val horaConexion: Long = System.currentTimeMillis(),
    val conectado: Boolean = true
)
