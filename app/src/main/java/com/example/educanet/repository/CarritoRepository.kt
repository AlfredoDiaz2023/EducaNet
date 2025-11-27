package com.example.educanet.repository

import com.example.educanet.model.Libro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoCarrito(
    val libros: List<Libro>,
    val ultimoDocumento: Any?
)

class CarritoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun actualizarStock(libroId: String, nuevoStock: Int): Boolean {
        return try {
            db.collection("libro")
                .document(libroId)
                .update("cantidad", nuevoStock)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}