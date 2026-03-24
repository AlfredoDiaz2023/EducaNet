package com.example.educanet.model

data class ProgresoAcademico(
    val id: String = "",
    val profesor: String = "",
    val profesorCorreo: String = "", // Correo del profesor para verificar edición
    val alumno: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val notas: Double = 0.0,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaModificacion: Long = System.currentTimeMillis()
)
