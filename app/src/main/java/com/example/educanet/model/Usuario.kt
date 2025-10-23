package com.example.educanet.model

data class Usuario (
    val correo: String = "",
    val clave: String = "",
    val nombre: String = "",
    val rol: String = "" // Variable local va a establecer si el usuario es admin, profesor, apoderado o alumno
)