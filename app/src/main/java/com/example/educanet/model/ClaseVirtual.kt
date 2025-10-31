package com.example.educanet.model

import com.google.firebase.firestore.IgnoreExtraProperties

@IgnoreExtraProperties
data class ClaseVirtual(
    val id: String = "",
    val nombre: String = "",
    val profesor: Profesor? = null,
    val nivel: String = "",
    val clase: String = "",
    val descripcion: String = ""

)