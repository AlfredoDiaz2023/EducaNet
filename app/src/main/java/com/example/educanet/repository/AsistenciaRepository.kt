package com.example.educanet.repository

import android.util.Log
import com.example.educanet.model.Asistencia
import com.example.educanet.model.Alumno
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class AsistenciaRepository {
    private val db = FirebaseFirestore.getInstance()
    private val asistenciaCollection = db.collection("asistencia")
    private val usuarioCollection = db.collection("usuario")

    // Obtener alumnos por curso
    suspend fun obtenerAlumnosPorCurso(curso: String): List<Alumno> {
        return try {
            val snapshot = usuarioCollection
                .whereEqualTo("rol", "Alumno")
                .whereEqualTo("curso", curso)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                Alumno(
                    id = doc.id,
                    nombre = data["nombre"] as? String ?: "",
                    correo = data["correo"] as? String ?: "",
                    clave = data["clave"] as? String ?: "",
                    rol = data["rol"] as? String ?: "Alumno",
                    fotoUrl = data["fotoUrl"] as? String ?: "",
                    fechaRegistro = data["fechaRegistro"] as? String ?: "",
                    curso = data["curso"] as? String ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Registrar asistencia de un alumno
    suspend fun registrarAsistencia(asistencia: Asistencia): Boolean {
        return try {
            val data = hashMapOf(
                "profesorId" to asistencia.profesorId,
                "profesorNombre" to asistencia.profesorNombre,
                "alumnoId" to asistencia.alumnoId,
                "alumnoNombre" to asistencia.alumnoNombre,
                "curso" to asistencia.curso,
                "asignaturaId" to asistencia.asignaturaId,
                "asignaturaNombre" to asistencia.asignaturaNombre,
                "fecha" to asistencia.fecha,
                "presente" to asistencia.presente,
                "justificacion" to asistencia.justificacion
            )
            asistenciaCollection.add(data).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Registrar asistencia masiva (lista completa de un curso)
    suspend fun registrarAsistenciaMasiva(listaAsistencia: List<Asistencia>): Boolean {
        return try {
            Log.d("AsistenciaRepo", "Registrando asistencia masiva: ${listaAsistencia.size} registros")
            val batch = db.batch()
            listaAsistencia.forEach { asistencia ->
                val docRef = asistenciaCollection.document()
                val data = hashMapOf(
                    "profesorId" to asistencia.profesorId,
                    "profesorNombre" to asistencia.profesorNombre,
                    "alumnoId" to asistencia.alumnoId,
                    "alumnoNombre" to asistencia.alumnoNombre,
                    "curso" to asistencia.curso,
                    "asignaturaId" to asistencia.asignaturaId,
                    "asignaturaNombre" to asistencia.asignaturaNombre,
                    "fecha" to asistencia.fecha,
                    "presente" to asistencia.presente,
                    "justificacion" to asistencia.justificacion
                )
                Log.d("AsistenciaRepo", "Guardando: alumnoId=${asistencia.alumnoId}, nombre=${asistencia.alumnoNombre}, asignatura=${asistencia.asignaturaNombre}, presente=${asistencia.presente}")
                batch.set(docRef, data)
            }
            batch.commit().await()
            Log.d("AsistenciaRepo", "Asistencia masiva guardada exitosamente")
            true
        } catch (e: Exception) {
            Log.e("AsistenciaRepo", "Error guardando asistencia masiva: ${e.message}")
            false
        }
    }

    // Obtener asistencia de un alumno específico
    suspend fun obtenerAsistenciaDeAlumno(alumnoId: String): List<Asistencia> {
        return try {
            Log.d("AsistenciaRepo", "Buscando asistencia para alumnoId: $alumnoId")
            
            // Primero intentamos con orderBy
            try {
                val snapshot = asistenciaCollection
                    .whereEqualTo("alumnoId", alumnoId)
                    .orderBy("fecha", Query.Direction.DESCENDING)
                    .get()
                    .await()
                Log.d("AsistenciaRepo", "Documentos encontrados con orderBy: ${snapshot.size()}")
                
                return snapshot.documents.mapNotNull { doc ->
                    parseAsistencia(doc)
                }
            } catch (indexError: Exception) {
                // Si falla por índice, intentamos sin orderBy
                Log.w("AsistenciaRepo", "Consulta con orderBy falló, intentando sin orden: ${indexError.message}")
                
                val snapshot = asistenciaCollection
                    .whereEqualTo("alumnoId", alumnoId)
                    .get()
                    .await()
                Log.d("AsistenciaRepo", "Documentos encontrados sin orderBy: ${snapshot.size()}")
                
                return snapshot.documents.mapNotNull { doc ->
                    parseAsistencia(doc)
                }.sortedByDescending { it.fecha }
            }
        } catch (e: Exception) {
            Log.e("AsistenciaRepo", "Error obteniendo asistencia: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }
    
    private fun parseAsistencia(doc: com.google.firebase.firestore.DocumentSnapshot): Asistencia? {
        return try {
            Asistencia(
                id = doc.id,
                profesorId = doc.getString("profesorId") ?: "",
                profesorNombre = doc.getString("profesorNombre") ?: "",
                alumnoId = doc.getString("alumnoId") ?: "",
                alumnoNombre = doc.getString("alumnoNombre") ?: "",
                curso = doc.getString("curso") ?: "",
                asignaturaId = doc.getString("asignaturaId") ?: "",
                asignaturaNombre = doc.getString("asignaturaNombre") ?: "",
                fecha = doc.getLong("fecha") ?: 0L,
                presente = doc.getBoolean("presente") ?: false,
                justificacion = doc.getString("justificacion") ?: ""
            )
        } catch (e: Exception) {
            Log.e("AsistenciaRepo", "Error parseando documento ${doc.id}: ${e.message}")
            null
        }
    }

    // Obtener asistencia por curso y fecha
    suspend fun obtenerAsistenciaPorCursoYFecha(curso: String, fechaInicio: Long, fechaFin: Long): List<Asistencia> {
        return try {
            val snapshot = asistenciaCollection
                .whereEqualTo("curso", curso)
                .whereGreaterThanOrEqualTo("fecha", fechaInicio)
                .whereLessThanOrEqualTo("fecha", fechaFin)
                .get()
                .await()
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(Asistencia::class.java)?.copy(id = doc.id)
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Verificar si ya se pasó asistencia hoy para un curso y asignatura específica
    suspend fun yaSeTomoAsistenciaHoy(curso: String, asignaturaId: String = ""): Boolean {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val inicioDelDia = calendar.timeInMillis
            
            calendar.add(Calendar.DAY_OF_MONTH, 1)
            val finDelDia = calendar.timeInMillis

            val query = if (asignaturaId.isNotEmpty()) {
                asistenciaCollection
                    .whereEqualTo("curso", curso)
                    .whereEqualTo("asignaturaId", asignaturaId)
                    .whereGreaterThanOrEqualTo("fecha", inicioDelDia)
                    .whereLessThan("fecha", finDelDia)
                    .limit(1)
            } else {
                asistenciaCollection
                    .whereEqualTo("curso", curso)
                    .whereGreaterThanOrEqualTo("fecha", inicioDelDia)
                    .whereLessThan("fecha", finDelDia)
                    .limit(1)
            }

            val snapshot = query.get().await()
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }

    // Calcular porcentaje de asistencia de un alumno
    suspend fun calcularPorcentajeAsistencia(alumnoId: String): Double {
        return try {
            val asistencias = obtenerAsistenciaDeAlumno(alumnoId)
            if (asistencias.isEmpty()) return 0.0
            
            val presentes = asistencias.count { it.presente }
            (presentes.toDouble() / asistencias.size) * 100
        } catch (e: Exception) {
            0.0
        }
    }

    // Obtener historial de asistencia por profesor
    suspend fun obtenerHistorialProfesor(profesorId: String): List<Asistencia> {
        return try {
            Log.d("AsistenciaRepo", "Buscando historial para profesorId: $profesorId")
            
            // Intentar con orderBy primero
            try {
                val snapshot = asistenciaCollection
                    .whereEqualTo("profesorId", profesorId)
                    .orderBy("fecha", Query.Direction.DESCENDING)
                    .limit(100)
                    .get()
                    .await()
                    
                Log.d("AsistenciaRepo", "Historial encontrado con orderBy: ${snapshot.size()} registros")
                return snapshot.documents.mapNotNull { doc ->
                    parseAsistencia(doc)
                }
            } catch (indexError: Exception) {
                Log.w("AsistenciaRepo", "Consulta con orderBy falló, intentando sin orden: ${indexError.message}")
                
                // Si falla, intentar sin orderBy
                val snapshot = asistenciaCollection
                    .whereEqualTo("profesorId", profesorId)
                    .limit(100)
                    .get()
                    .await()
                    
                Log.d("AsistenciaRepo", "Historial encontrado sin orderBy: ${snapshot.size()} registros")
                return snapshot.documents.mapNotNull { doc ->
                    parseAsistencia(doc)
                }.sortedByDescending { it.fecha }
            }
        } catch (e: Exception) {
            Log.e("AsistenciaRepo", "Error obteniendo historial profesor: ${e.message}")
            emptyList()
        }
    }
}
