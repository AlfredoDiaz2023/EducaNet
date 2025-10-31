package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Tutoria(
    val id: String = "",
    val nombre: String = "",
    val profesor: Profesor? = null,
    val alumno: Alumno? = null,
    val duracion: Int = 0,
    val descripcion: String = ""
)
