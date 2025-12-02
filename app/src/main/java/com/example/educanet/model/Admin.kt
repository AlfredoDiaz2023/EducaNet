package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Administrador(
    override val id: String = "",
    override val nombre: String = "", // El orden importa: id, nombre...
    override val correo: String = "",
    override val clave: String = "",
    override val rol: String = "Administrador",
    override val fotoUrl: String = "", // Faltaba este campo que está en Usuario

    val fechaRegistro: String = ""
) : Usuario(id, nombre, correo, clave, rol, fotoUrl) // Pasamos todos