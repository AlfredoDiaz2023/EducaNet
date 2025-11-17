package com.example.educanet.model

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class Resena(
    val id: String = "",
    val itemId: String = "", // ID del libro, video, etc.
    val userId: String = "",
    val userName: String = "",
    val rating: Float = 0.0f,
    val comment: String = "",
    @ServerTimestamp
    val timestamp: Date? = null
)
