package com.example.educanet.repository

import com.example.educanet.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ReservaRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarReserva(reserva: Reserva): Boolean {
        return try {
            db.collection("reservas").add(reserva).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
