package com.example.educanet.model

data class Profesor (
    val correo: String = "",
    val clave: String = "",
    val nombre: String = "",
    val rol: String = "Profesor" // Variable local va a establecer si el usuario es admin, profesor, apoderado o alumno
)