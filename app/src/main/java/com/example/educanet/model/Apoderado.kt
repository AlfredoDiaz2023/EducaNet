package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Apoderado(
    override val id: String = "",
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Apoderado",
    override val fotoUrl: String = "",// <-- CORRECCIÓN (Hereda)
    val fechaRegistro: String = ""
) : Usuario(id, nombre, correo, clave, rol, fotoUrl)