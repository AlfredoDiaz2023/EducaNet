package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Profesor(
    override val id: String = "",
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Profesor",
    override val fotoUrl: String? = null, // <-- CORRECCIÓN (Hereda)
    val fechaRegistro: String = ""
) : Usuario(id, correo, clave, nombre, rol)