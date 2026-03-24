package com.example.educanet.repository

import android.util.Log
import com.example.educanet.model.Asignatura
import com.example.educanet.model.AsignaturasPredefinidas
import com.example.educanet.model.NivelEducativo
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AsignaturaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val asignaturasCollection = db.collection("asignaturas")

    /**
     * Obtiene todas las asignaturas de Firebase
     */
    suspend fun obtenerTodasLasAsignaturas(): List<Asignatura> {
        return try {
            val snapshot = asignaturasCollection
                .whereEqualTo("activa", true)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Asignatura(
                        id = doc.id,
                        nombre = data["nombre"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        nivelEducativo = try {
                            NivelEducativo.valueOf(data["nivelEducativo"] as? String ?: "TODOS")
                        } catch (e: Exception) {
                            NivelEducativo.TODOS
                        },
                        cursos = (data["cursos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        esObligatoria = data["esObligatoria"] as? Boolean ?: true,
                        activa = data["activa"] as? Boolean ?: true,
                        fechaCreacion = (data["fechaCreacion"] as? Long) ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e("AsignaturaRepo", "Error parseando asignatura: ${e.message}")
                    null
                }
            }.sortedBy { it.nombre }
        } catch (e: Exception) {
            Log.e("AsignaturaRepo", "Error obteniendo asignaturas: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtiene asignaturas por curso específico
     */
    suspend fun obtenerAsignaturasPorCurso(curso: String): List<Asignatura> {
        return try {
            val snapshot = asignaturasCollection
                .whereEqualTo("activa", true)
                .whereArrayContains("cursos", curso)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    Asignatura(
                        id = doc.id,
                        nombre = data["nombre"] as? String ?: "",
                        descripcion = data["descripcion"] as? String ?: "",
                        nivelEducativo = try {
                            NivelEducativo.valueOf(data["nivelEducativo"] as? String ?: "TODOS")
                        } catch (e: Exception) {
                            NivelEducativo.TODOS
                        },
                        cursos = (data["cursos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                        esObligatoria = data["esObligatoria"] as? Boolean ?: true,
                        activa = data["activa"] as? Boolean ?: true,
                        fechaCreacion = (data["fechaCreacion"] as? Long) ?: System.currentTimeMillis()
                    )
                } catch (e: Exception) {
                    Log.e("AsignaturaRepo", "Error parseando asignatura: ${e.message}")
                    null
                }
            }.sortedBy { it.nombre }
        } catch (e: Exception) {
            Log.e("AsignaturaRepo", "Error obteniendo asignaturas por curso: ${e.message}")
            emptyList()
        }
    }

    /**
     * Crea una nueva asignatura
     */
    suspend fun crearAsignatura(asignatura: Asignatura): Boolean {
        return try {
            val data = hashMapOf(
                "nombre" to asignatura.nombre,
                "descripcion" to asignatura.descripcion,
                "nivelEducativo" to asignatura.nivelEducativo.name,
                "cursos" to asignatura.cursos,
                "esObligatoria" to asignatura.esObligatoria,
                "activa" to true,
                "fechaCreacion" to System.currentTimeMillis()
            )
            asignaturasCollection.add(data).await()
            Log.d("AsignaturaRepo", "Asignatura creada: ${asignatura.nombre}")
            true
        } catch (e: Exception) {
            Log.e("AsignaturaRepo", "Error creando asignatura: ${e.message}")
            false
        }
    }

    /**
     * Actualiza una asignatura existente
     */
    suspend fun actualizarAsignatura(asignatura: Asignatura): Boolean {
        return try {
            val data = hashMapOf(
                "nombre" to asignatura.nombre,
                "descripcion" to asignatura.descripcion,
                "nivelEducativo" to asignatura.nivelEducativo.name,
                "cursos" to asignatura.cursos,
                "esObligatoria" to asignatura.esObligatoria,
                "activa" to asignatura.activa
            )
            asignaturasCollection.document(asignatura.id).update(data as Map<String, Any>).await()
            Log.d("AsignaturaRepo", "Asignatura actualizada: ${asignatura.nombre}")
            true
        } catch (e: Exception) {
            Log.e("AsignaturaRepo", "Error actualizando asignatura: ${e.message}")
            false
        }
    }

    /**
     * Elimina (desactiva) una asignatura
     */
    suspend fun eliminarAsignatura(asignaturaId: String): Boolean {
        return try {
            asignaturasCollection.document(asignaturaId)
                .update("activa", false)
                .await()
            Log.d("AsignaturaRepo", "Asignatura eliminada: $asignaturaId")
            true
        } catch (e: Exception) {
            Log.e("AsignaturaRepo", "Error eliminando asignatura: ${e.message}")
            false
        }
    }

    /**
     * Inicializa las asignaturas predefinidas en Firebase si no existen
     */
    suspend fun inicializarAsignaturasPredefinidas(): Boolean {
        return try {
            val existentes = obtenerTodasLasAsignaturas()
            
            if (existentes.isEmpty()) {
                Log.d("AsignaturaRepo", "Inicializando asignaturas predefinidas...")
                
                val batch = db.batch()
                AsignaturasPredefinidas.todasLasAsignaturas.forEach { asignatura ->
                    val docRef = asignaturasCollection.document()
                    val data = hashMapOf(
                        "nombre" to asignatura.nombre,
                        "descripcion" to asignatura.descripcion,
                        "nivelEducativo" to asignatura.nivelEducativo.name,
                        "cursos" to asignatura.cursos,
                        "esObligatoria" to asignatura.esObligatoria,
                        "activa" to true,
                        "fechaCreacion" to System.currentTimeMillis()
                    )
                    batch.set(docRef, data)
                }
                batch.commit().await()
                Log.d("AsignaturaRepo", "Asignaturas predefinidas inicializadas correctamente")
                true
            } else {
                Log.d("AsignaturaRepo", "Ya existen ${existentes.size} asignaturas")
                true
            }
        } catch (e: Exception) {
            Log.e("AsignaturaRepo", "Error inicializando asignaturas: ${e.message}")
            false
        }
    }

    /**
     * Obtiene una asignatura por su ID
     */
    suspend fun obtenerAsignaturaPorId(id: String): Asignatura? {
        return try {
            val doc = asignaturasCollection.document(id).get().await()
            val data = doc.data ?: return null
            
            Asignatura(
                id = doc.id,
                nombre = data["nombre"] as? String ?: "",
                descripcion = data["descripcion"] as? String ?: "",
                nivelEducativo = try {
                    NivelEducativo.valueOf(data["nivelEducativo"] as? String ?: "TODOS")
                } catch (e: Exception) {
                    NivelEducativo.TODOS
                },
                cursos = (data["cursos"] as? List<*>)?.filterIsInstance<String>() ?: emptyList(),
                esObligatoria = data["esObligatoria"] as? Boolean ?: true,
                activa = data["activa"] as? Boolean ?: true,
                fechaCreacion = (data["fechaCreacion"] as? Long) ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("AsignaturaRepo", "Error obteniendo asignatura por ID: ${e.message}")
            null
        }
    }
}
