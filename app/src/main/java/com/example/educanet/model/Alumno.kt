package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Alumno(
    // Campos obligatorios que se reescriben de Usuario
    override val id: String = "",
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Alumno",

    // Campos específicos de Alumno
    val fechaRegistro: String = "",
    val curso: String = "",
    override val fotoUrl: String? = null // <-- CORRECCIÓN (Hereda)
) : Usuario(id, correo, clave, nombre, rol) // Hereda de la clase base