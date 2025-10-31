package com.example.educanet.repository

import com.example.educanet.model.ProgresoAcademico
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoProgresosAcademicos(
    val progresos: List<ProgresoAcademico>,
    val ultimoDocumento: Any?
)

class ProgresoAcademicoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerProgresosAcademicos(limite: Int = 10): ResultadoProgresosAcademicos {
        return try {
            val query = db.collection("progresos_academicos")
                .orderBy("asignatura", Query.Direction.ASCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val progresos = querySnapshot.toObjects(ProgresoAcademico::class.java)

            val ultimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoProgresosAcademicos(progresos, ultimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoProgresosAcademicos(emptyList(), null)
        }
    }

    suspend fun obtenerMasProgresosAcademicos(limite: Int = 10, ultimoDocumento: Any?): ResultadoProgresosAcademicos {
        return try {
            if (ultimoDocumento == null) return ResultadoProgresosAcademicos(emptyList(), null)

            val query = db.collection("progresos_academicos")
                .orderBy("asignatura", Query.Direction.ASCENDING)
                .startAfter(ultimoDocumento)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val progresos = querySnapshot.toObjects(ProgresoAcademico::class.java)

            val nuevoUltimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoProgresosAcademicos(progresos, nuevoUltimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoProgresosAcademicos(emptyList(), null)
        }
    }

    suspend fun obtenerProgresoAcademicoPorId(progresoId: String): ProgresoAcademico? {
        return try {
            db.collection("progresos_academicos")
                .document(progresoId)
                .get()
                .await()
                .toObject(ProgresoAcademico::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
