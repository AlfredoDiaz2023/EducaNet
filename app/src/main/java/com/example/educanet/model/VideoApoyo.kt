package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class VideoApoyo(
    val id: String = "",
    val nombre: String = "",
    val profesor: Profesor? = null,
    val nivel: String = "",
    val video: String = "",
    val descripcion: String = "",
    val duracion: Int = 0
)
