package com.example.educanet.repository

import com.example.educanet.model.ClaseVirtual
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoClasesVirtuales(
    val clases: List<ClaseVirtual>,
    val ultimoDocumento: Any?
)

class ClaseVirtualRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerClasesVirtuales(limite: Int = 10): ResultadoClasesVirtuales {
        return try {
            val query = db.collection("clases_virtuales")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val clases = querySnapshot.toObjects(ClaseVirtual::class.java)

            val ultimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoClasesVirtuales(clases, ultimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoClasesVirtuales(emptyList(), null)
        }
    }

    suspend fun obtenerMasClasesVirtuales(limite: Int = 10, ultimoDocumento: Any?): ResultadoClasesVirtuales {
        return try {
            if (ultimoDocumento == null) return ResultadoClasesVirtuales(emptyList(), null)

            val query = db.collection("clases_virtuales")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .startAfter(ultimoDocumento)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val clases = querySnapshot.toObjects(ClaseVirtual::class.java)

            val nuevoUltimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoClasesVirtuales(clases, nuevoUltimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoClasesVirtuales(emptyList(), null)
        }
    }

    suspend fun obtenerClaseVirtualPorId(claseId: String): ClaseVirtual? {
        return try {
            db.collection("clases_virtuales")
                .document(claseId)
                .get()
                .await()
                .toObject(ClaseVirtual::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
