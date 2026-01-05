package com.example.educanet.model

data class Asistencia(
    val id: String = "",
    val profesorId: String = "",
    val profesorNombre: String = "",
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val curso: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val presente: Boolean = false,
    val justificacion: String = "" // Para ausencias justificadas
)
