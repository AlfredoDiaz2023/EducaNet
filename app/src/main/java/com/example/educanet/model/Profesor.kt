package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Profesor(
    override val id: String = "",
    override val nombre: String = "", // El orden importa para coincidir con Usuario
    override val correo: String = "",
    override val clave: String = "",
    override val rol: String = "Profesor",
    override val fotoUrl: String = "", // Debe ser String (no null) para coincidir con Usuario
    val fechaRegistro: String = ""
) : Usuario(id, nombre, correo, clave, rol, fotoUrl)