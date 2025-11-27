package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties


@IgnoreExtraProperties
abstract class Usuario(
    open val id: String = "",
    open val correo: String = "",
    open val clave: String = "",
    open val nombre: String = "",
    open val rol: String = "",
    open val fotoUrl: String? = null
)