package com.example.educanet.model

data class ProgresoAcademico(
    val id: String = "",
    val profesor: Profesor? = null,
    val alumno: Alumno? = null,
    val asignatura: String = "",
    val curso: String = "",
    val notas: String = ""
)
