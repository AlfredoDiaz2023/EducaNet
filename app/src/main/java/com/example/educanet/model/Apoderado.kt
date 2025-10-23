package com.example.educanet.model

data class Apoderado (
    val correo: String = "",
    val clave: String = "",
    val nombre: String = "",
    val rol: String = "Apoderado" // Variable local va a establecer si el usuario es admin, profesor, apoderado o alumno
)