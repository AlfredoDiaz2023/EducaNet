package com.example.educanet.repository

import com.example.educanet.model.ProgresoAcademico
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoNotas(
    val progresoAcademico: List<ProgresoAcademico>,
    val ultimoDocumento: Any?
)

class ProgresoAcademicoRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarNota(progresoAcademico: ProgresoAcademico): Boolean {
        return try {
            db.collection("notas")
                .add(progresoAcademico)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerNotas(userEmail: String? = null, userRole: String? = null, limite: Int = 20): ResultadoNotas {
        return try {
            var query: Query = db.collection("notas")

            if (userRole == "alumno" && userEmail != null) {
                query = query.whereEqualTo("alumno", userEmail)
            }

            val querySnapshot = query.orderBy("notas", Query.Direction.DESCENDING)
                .limit(limite.toLong())
                .get()
                .await()
            val lista = querySnapshot.documents.map { doc ->
                ProgresoAcademico(
                    id = doc.id,
                    profesor = doc.getString("profesor") ?: "",
                    alumno = doc.getString("alumno") ?: "",
                    asignatura = doc.getString("asignatura") ?: "",
                    curso = doc.getString("curso") ?: "",
                    notas = doc.getDouble("notas") ?: 0.0
                )
            }

            val ultimo = querySnapshot.documents.lastOrNull()
            ResultadoNotas(lista, ultimo)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoNotas(emptyList(), null)
        }
    }
}
