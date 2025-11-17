package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

data class Libro(
    val id: String = "",
    val nombre: String = "",
    val nivel: String = "",
    val imagen: String = "",
    val cantidad: Int = 0
)

