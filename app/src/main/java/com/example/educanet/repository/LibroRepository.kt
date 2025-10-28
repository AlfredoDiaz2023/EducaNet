package com.example.educanet.repository

import com.example.educanet.model.Libro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoLibros(
    val libros: List<Libro>,
    val ultimoDocumento: Any?
)

class LibroRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun obtenerLibros(limite: Int = 10): ResultadoLibros {
        return try {
            val query = db.collection("libro")
                .whereGreaterThan("cantidad", 0)
                .orderBy("cantidad", Query.Direction.DESCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val libros = querySnapshot.documents.map { document ->
                Libro(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    nivel = document.getString("nivel") ?: "",
                    imagen = document.getString("imagen") ?: "",
                    cantidad = document.getLong("cantidad")?.toInt() ?: 0
                )
            }

            val ultimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoLibros(libros, ultimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace() // 👈 imprime el error para debug
            ResultadoLibros(emptyList(), null)
        }
    }

    suspend fun obtenerMasLibros(limite: Int = 10, ultimoDocumento: Any?): ResultadoLibros {
        return try {
            if (ultimoDocumento == null) return ResultadoLibros(emptyList(), null)

            val query = db.collection("libros")
                .whereGreaterThan("cantidad", 0)
                .orderBy("cantidad", Query.Direction.DESCENDING)
                .startAfter(ultimoDocumento)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val libros = querySnapshot.documents.map { document ->
                Libro(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    nivel = document.getString("nivel") ?: "",
                    imagen = document.getString("imagen") ?: "",
                    cantidad = document.getLong("cantidad")?.toInt() ?: 0
                )
            }

            val nuevoUltimoDocumento = if (querySnapshot.documents.isNotEmpty()) {
                querySnapshot.documents.last()
            } else {
                null
            }

            ResultadoLibros(libros, nuevoUltimoDocumento)
        } catch (e: Exception) {
            e.printStackTrace()
            ResultadoLibros(emptyList(), null)
        }
    }

    suspend fun actualizarStock(libroId: String, nuevoStock: Int): Boolean {
        return try {
            db.collection("libros")
                .document(libroId)
                .update("cantidad", nuevoStock)
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun obtenerLibroPorId(libroId: String): Libro? {
        return try {
            val document = db.collection("libros") //
                .document(libroId)
                .get()
                .await()

            if (document.exists()) {
                Libro(
                    id = document.id,
                    nombre = document.getString("nombre") ?: "",
                    nivel = document.getString("nivel") ?: "",
                    imagen = document.getString("imagen") ?: "",
                    cantidad = document.getLong("cantidad")?.toInt() ?: 0
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
