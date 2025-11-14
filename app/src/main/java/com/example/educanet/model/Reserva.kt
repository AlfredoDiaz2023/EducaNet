package com.example.educanet.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Reserva(
    val id: String = "",
    val libroId: String = "",
    val userId: String = "",
    val userName: String = "",
    val libroNombre: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
