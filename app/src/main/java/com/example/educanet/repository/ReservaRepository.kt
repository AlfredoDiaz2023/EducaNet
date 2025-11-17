package com.example.educanet.repository

import com.example.educanet.model.Reserva
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
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

    suspend fun obtenerReservas(): List<Reserva> {
        return try {
            val snapshot = db.collection("reservas")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.documents.mapNotNull {
                it.toObject(Reserva::class.java)?.copy(id = it.id)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun eliminarReserva(id: String): Boolean {
        return try {
            db.collection("reservas").document(id).delete().await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
