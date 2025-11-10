package com.example.educanet.repository

import com.example.educanet.model.Resena
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class ResenaRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarResena(resena: Resena): Boolean {
        return try {
            db.collection("resenas").add(resena).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerResenas(itemId: String): List<Resena> {
        return try {
            val snapshot = db.collection("resenas")
                .whereEqualTo("itemId", itemId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            snapshot.toObjects(Resena::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
