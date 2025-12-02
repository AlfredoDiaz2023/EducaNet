package com.example.educanet.repository

import android.util.Log
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
    private val collectionName = "progreso_academico"

    suspend fun agregarNota(progresoAcademico: ProgresoAcademico): Boolean {
        return try {
            Log.d("ProgresoRepo", "Guardando nota para alumno: ${progresoAcademico.alumno}")
            db.collection(collectionName)
                .add(progresoAcademico)
                .await()
            Log.d("ProgresoRepo", "Nota guardada exitosamente")
            true
        } catch (e: Exception) {
            Log.e("ProgresoRepo", "Error al guardar nota: ${e.message}")
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerNotas(
        userEmail: String? = null,
        userRole: String? = null,
        limite: Int = 50
    ): ResultadoNotas {
        return try {
            Log.d("ProgresoRepo", "Buscando notas - Email: $userEmail, Rol: $userRole")
            
            var query: Query = db.collection(collectionName)

            // Si es alumno, filtrar por su correo
            if (userRole?.equals("Alumno", ignoreCase = true) == true && userEmail != null) {
                Log.d("ProgresoRepo", "Filtrando por alumno con correo: $userEmail")
                query = query.whereEqualTo("alumno", userEmail)
            }

            // Ejecutar la query sin ordenamiento para evitar necesidad de índice compuesto
            val querySnapshot = query
                .limit(limite.toLong())
                .get()
                .await()

            Log.d("ProgresoRepo", "Documentos encontrados: ${querySnapshot.documents.size}")

            // Mapear documentos a objetos
            val lista = querySnapshot.documents.mapNotNull { doc ->
                val progreso = doc.toObject(ProgresoAcademico::class.java)?.copy(id = doc.id)
                Log.d("ProgresoRepo", "Documento: id=${doc.id}, alumno=${doc.getString("alumno")}, nota=${doc.getDouble("notas")}")
                progreso
            }.sortedByDescending { it.notas } // Ordenar en memoria

            Log.d("ProgresoRepo", "Notas procesadas: ${lista.size}")
            
            val ultimo = querySnapshot.documents.lastOrNull()
            ResultadoNotas(lista, ultimo)
        } catch (e: Exception) {
            Log.e("ProgresoRepo", "Error al obtener notas: ${e.message}")
            e.printStackTrace()
            ResultadoNotas(emptyList(), null)
        }
    }
}