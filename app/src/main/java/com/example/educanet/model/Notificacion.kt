package com.example.educanet.model

data class Notificacion(
    val id: String = "",
    val titulo: String = "",
    val mensaje: String = "",
    val fecha: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)