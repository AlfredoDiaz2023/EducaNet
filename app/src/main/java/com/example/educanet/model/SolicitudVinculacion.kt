package com.example.educanet.model

data class SolicitudVinculacion(
    val id: String = "",
    val apoderadoId: String = "",
    val apoderadoNombre: String = "",
    val apoderadoCorreo: String = "",
    val alumnoId: String = "",
    val alumnoNombre: String = "",
    val alumnoCorreo: String = "",
    val fechaSolicitud: Long = System.currentTimeMillis(),
    val estado: String = "pendiente" // pendiente, aprobada, rechazada
)
