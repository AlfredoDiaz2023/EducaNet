package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

// Estructura simple para el profesor
@IgnoreExtraProperties
data class ProfesorSimple(
    val correo: String = "",
    val nombre: String = "",
    val rol: String = ""
)

@IgnoreExtraProperties
data class VideoApoyo(
    val id: String = "",
    val nombre: String = "",
    val nivel: String = "",
    val curso: String = "",  // Curso al que va dirigido (ej: "4° Básico")
    val video: String = "",
    val descripcion: String = "",
    val duracion: Int = 0,
    val profesor: ProfesorSimple = ProfesorSimple()
)
