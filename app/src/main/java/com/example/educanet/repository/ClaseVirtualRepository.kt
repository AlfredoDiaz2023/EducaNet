package com.example.educanet.repository

import com.example.educanet.model.ClaseVirtual
import com.example.educanet.model.ProfesorSimple
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

data class ResultadoClasesVirtuales(
    val clases: List<ClaseVirtual>,
    val ultimoDocumento: Any?
)

class ClaseVirtualRepository {
    private val db = FirebaseFirestore.getInstance()

    suspend fun agregarClaseVirtual(claseVirtual: ClaseVirtual): Boolean {
        return try {
            // Crear estructura plana para Firestore
            val claseData = hashMapOf(
                "nombre" to claseVirtual.nombre,
                "descripcion" to claseVirtual.descripcion,
                "duracion" to claseVirtual.duracion,
                "nivel" to claseVirtual.nivel,
                "clase" to claseVirtual.clase,
                "meet" to claseVirtual.meet,
                "profesor" to hashMapOf(
                    "correo" to claseVirtual.profesor.correo,
                    "nombre" to claseVirtual.profesor.nombre,
                    "rol" to claseVirtual.profesor.rol
                )
            )
            db.collection("clases_virtuales").add(claseData).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Función auxiliar para mapear documento a ClaseVirtual de forma segura
    private fun documentToClaseVirtual(doc: com.google.firebase.firestore.DocumentSnapshot): ClaseVirtual? {
        return try {
            val data = doc.data ?: return null
            
            // Extraer profesor de forma segura
            val profesorData = data["profesor"] as? Map<*, *>
            val profesorSimple = if (profesorData != null) {
                ProfesorSimple(
                    correo = profesorData["correo"]?.toString() ?: "",
                    nombre = profesorData["nombre"]?.toString() ?: "",
                    rol = profesorData["rol"]?.toString() ?: ""
                )
            } else {
                ProfesorSimple()
            }
            
            ClaseVirtual(
                id = doc.id,
                nombre = data["nombre"]?.toString() ?: "",
                profesor = profesorSimple,
                nivel = data["nivel"]?.toString() ?: "",
                clase = data["clase"]?.toString() ?: "",
                descripcion = data["descripcion"]?.toString() ?: "",
                meet = data["meet"]?.toString() ?: "",
                duracion = (data["duracion"] as? Number)?.toInt() ?: 0
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun obtenerClasesVirtuales(limite: Int = 10): ResultadoClasesVirtuales {
        return try {
            val query = db.collection("clases_virtuales")
                .orderBy("nombre", Query.Direction.ASCENDING)
                .limit(limite.toLong())

            val querySnapshot = query.get().await()
            val clases = querySnapshot.documents.mapNotNull { doc ->
                documentToClaseVirtual(doc)
            }

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
            val clases = querySnapshot.documents.mapNotNull { doc ->
                documentToClaseVirtual(doc)
            }

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
            val doc = db.collection("clases_virtuales")
                .document(claseId)
                .get()
                .await()
            documentToClaseVirtual(doc)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
