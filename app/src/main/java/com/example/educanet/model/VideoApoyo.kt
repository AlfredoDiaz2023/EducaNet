package com.example.educanet.model

data class VideoApoyo(
    val id: String = "",
    val nombre: String = "",
    val profesor: Profesor,
    val nivel: String = "",
    val video: String = "",
    val descripcion: String = "",
    val duracion: Int = 0
)