package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class Administrador(
    // Campos obligatorios que se reescriben de Usuario
    override val id: String = "",
    override val correo: String = "",
    override val clave: String = "",
    override val nombre: String = "",
    override val rol: String = "Administrador",

    // Campos específicos de Administrador (si los hay)
    val fechaRegistro: String = "", // Visto en tu AdministradorRepository
    override val fotoUrl: String? = null // <-- CORRECCIÓN (Hereda)
) : Usuario(id, correo, clave, nombre, rol) // Hereda de la clase base