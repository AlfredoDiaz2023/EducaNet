package com.example.educanet.repository

import com.example.educanet.model.Tutoria
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoTutorias(
    val tutorias: List<Tutoria>,
    val ultimoDocumento: Any?
)

class TutoriaRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerTutorias(limite: Int = 10): ResultadoTutorias {
        return try {
            val query = db.collection("tutorias")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val tutorias = querySnapshot.toObjects(Tutoria::class.java)

            val ultimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoTutorias(tutorias, ultimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoTutorias(emptyList(), null)
        }
    }

    suspend fun obtenerMasTutorias(limite: Int = 10, ultimoDocumento: Any?): ResultadoTutorias {
        return try {
            if (ultimoDocumento == null) return ResultadoTutorias(emptyList(), null)

            val query = db.collection("tutorias")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .startAfter(ultimoDocumento)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val tutorias = querySnapshot.toObjects(Tutoria::class.java)

            val nuevoUltimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoTutorias(tutorias, nuevoUltimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoTutorias(emptyList(), null)
        }
    }

    suspend fun obtenerTutoriaPorId(tutoriaId: String): Tutoria? {
        return try {
            db.collection("tutorias")
                .document(tutoriaId)
                .get()
                .await()
                .toObject(Tutoria::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
