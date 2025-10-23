package com.example.educanet.model

data class Administrador (
    val correo: String = "",
    val clave: String = "",
    val nombre: String = "",
    val rol: String = "Administrador" // Variable local va a establecer si el usuario es admin, profesor, apoderado o alumno
)