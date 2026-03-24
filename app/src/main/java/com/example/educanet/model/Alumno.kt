package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Alumno(
    override val id: String = "",
    override val nombre: String = "",
    override val correo: String = "",
    override val clave: String = "",
    override val rol: String = "Alumno",
    override val fotoUrl: String = "",
    val fechaRegistro: String = "",
    val curso: String = "" // Curso del alumno (1° Básico a 4° Medio)
) : Usuario(id, nombre, correo, clave, rol, fotoUrl)