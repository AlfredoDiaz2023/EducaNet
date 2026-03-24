package com.example.educanet.repository

import android.util.Log
import com.example.educanet.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class HorarioRepository {
    private val db = FirebaseFirestore.getInstance()
    private val horariosCollection = db.collection("horarios")

    /**
     * Obtiene todos los horarios escolares
     */
    suspend fun obtenerTodosLosHorarios(): List<HorarioEscolar> {
        return try {
            val snapshot = horariosCollection
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                parseHorarioFromDocument(doc.id, doc.data)
            }.sortedBy { it.curso }
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error obteniendo horarios: ${e.message}")
            emptyList()
        }
    }

    /**
     * Obtiene el horario de un curso específico
     */
    suspend fun obtenerHorarioPorCurso(curso: String): HorarioEscolar? {
        return try {
            val snapshot = horariosCollection
                .whereEqualTo("curso", curso)
                .whereEqualTo("activo", true)
                .get()
                .await()
            
            if (!snapshot.isEmpty) {
                val doc = snapshot.documents.first()
                parseHorarioFromDocument(doc.id, doc.data)
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error obteniendo horario por curso: ${e.message}")
            null
        }
    }

    /**
     * Crea o actualiza el horario de un curso
     */
    suspend fun guardarHorario(horario: HorarioEscolar): Boolean {
        return try {
            // Verificar si ya existe un horario para este curso
            val existente = obtenerHorarioPorCurso(horario.curso)
            
            val horarioData = hashMapOf(
                "curso" to horario.curso,
                "anioEscolar" to horario.anioEscolar,
                "horaEntradaGeneral" to horario.horaEntradaGeneral,
                "horarioSemanal" to horario.horarioSemanal.map { (dia, horarioDia) ->
                    mapOf(
                        "diaSemana" to dia.name,
                        "horaEntrada" to horarioDia.horaEntrada,
                        "horaSalida" to horarioDia.horaSalida,
                        "bloques" to horarioDia.bloques.map { bloque ->
                            mapOf(
                                "id" to bloque.id,
                                "asignatura" to bloque.asignatura,
                                "horaInicio" to bloque.horaInicio,
                                "horaFin" to bloque.horaFin,
                                "profesor" to bloque.profesor,
                                "sala" to bloque.sala
                            )
                        }
                    )
                },
                "activo" to horario.activo,
                "fechaCreacion" to (existente?.fechaCreacion ?: System.currentTimeMillis()),
                "fechaModificacion" to System.currentTimeMillis()
            )
            
            if (existente != null) {
                // Actualizar existente
                horariosCollection.document(existente.id).set(horarioData).await()
            } else {
                // Crear nuevo
                horariosCollection.add(horarioData).await()
            }
            
            Log.d("HorarioRepo", "Horario guardado para curso: ${horario.curso}")
            true
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error guardando horario: ${e.message}")
            false
        }
    }

    /**
     * Actualiza la hora de salida de un día específico para un curso
     */
    suspend fun actualizarHoraSalida(curso: String, diaSemana: DiaSemana, nuevaHoraSalida: String): Boolean {
        return try {
            val horarioActual = obtenerHorarioPorCurso(curso) ?: return false
            
            val nuevoHorarioSemanal = horarioActual.horarioSemanal.toMutableMap()
            nuevoHorarioSemanal[diaSemana] = horarioActual.horarioSemanal[diaSemana]?.copy(
                horaSalida = nuevaHoraSalida
            ) ?: HorarioDia(diaSemana, "08:00", nuevaHoraSalida)
            
            val horarioActualizado = horarioActual.copy(
                horarioSemanal = nuevoHorarioSemanal,
                fechaModificacion = System.currentTimeMillis()
            )
            
            guardarHorario(horarioActualizado)
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error actualizando hora salida: ${e.message}")
            false
        }
    }

    /**
     * Elimina (desactiva) el horario de un curso
     */
    suspend fun eliminarHorario(horarioId: String): Boolean {
        return try {
            horariosCollection.document(horarioId)
                .update("activo", false)
                .await()
            Log.d("HorarioRepo", "Horario eliminado: $horarioId")
            true
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error eliminando horario: ${e.message}")
            false
        }
    }

    /**
     * Inicializa los horarios predeterminados CON ASIGNATURAS para todos los cursos si no existen
     * Utiliza el GeneradorHorarioConAsignaturas para crear horarios con materias ya distribuidas
     */
    suspend fun inicializarHorariosPredeterminados(): Boolean {
        return try {
            val cursosExistentes = obtenerTodosLosHorarios().map { it.curso }.toSet()
            
            Cursos.lista.forEach { curso ->
                if (curso !in cursosExistentes) {
                    // Usar el generador que incluye asignaturas predistribuidas
                    val horarioConAsignaturas = GeneradorHorarioConAsignaturas.generarHorarioConAsignaturas(curso)
                    
                    val horarioPredeterminado = HorarioEscolar(
                        curso = curso,
                        anioEscolar = 2026,
                        horaEntradaGeneral = "08:00",
                        horarioSemanal = horarioConAsignaturas
                    )
                    guardarHorario(horarioPredeterminado)
                    Log.d("HorarioRepo", "Horario con asignaturas creado para: $curso")
                }
            }
            
            Log.d("HorarioRepo", "Horarios predeterminados con asignaturas inicializados")
            true
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error inicializando horarios: ${e.message}")
            false
        }
    }
    
    /**
     * Reinicializa TODOS los horarios con asignaturas predistribuidas
     * Útil para actualizar horarios existentes que no tenían asignaturas
     */
    suspend fun reinicializarHorariosConAsignaturas(): Boolean {
        return try {
            Cursos.lista.forEach { curso ->
                val horarioConAsignaturas = GeneradorHorarioConAsignaturas.generarHorarioConAsignaturas(curso)
                
                val horarioExistente = obtenerHorarioPorCurso(curso)
                
                val horarioActualizado = HorarioEscolar(
                    id = horarioExistente?.id ?: "",
                    curso = curso,
                    anioEscolar = 2026,
                    horaEntradaGeneral = "08:00",
                    horarioSemanal = horarioConAsignaturas,
                    fechaCreacion = horarioExistente?.fechaCreacion ?: System.currentTimeMillis(),
                    fechaModificacion = System.currentTimeMillis()
                )
                guardarHorario(horarioActualizado)
                Log.d("HorarioRepo", "Horario reinicializado con asignaturas para: $curso")
            }
            
            Log.d("HorarioRepo", "Todos los horarios reinicializados con asignaturas")
            true
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error reinicializando horarios: ${e.message}")
            false
        }
    }

    /**
     * Parsea un documento de Firestore a un objeto HorarioEscolar
     */
    @Suppress("UNCHECKED_CAST")
    private fun parseHorarioFromDocument(docId: String, data: Map<String, Any>?): HorarioEscolar? {
        if (data == null) return null
        
        return try {
            val horarioSemanalData = data["horarioSemanal"] as? List<Map<String, Any>> ?: emptyList()
            
            val horarioSemanal = mutableMapOf<DiaSemana, HorarioDia>()
            
            horarioSemanalData.forEach { diaData ->
                try {
                    val diaSemana = DiaSemana.valueOf(diaData["diaSemana"] as? String ?: "LUNES")
                    val bloquesData = diaData["bloques"] as? List<Map<String, Any>> ?: emptyList()
                    
                    val bloques = bloquesData.map { bloqueData ->
                        BloqueHorario(
                            id = bloqueData["id"] as? String ?: "",
                            asignatura = bloqueData["asignatura"] as? String ?: "",
                            horaInicio = bloqueData["horaInicio"] as? String ?: "",
                            horaFin = bloqueData["horaFin"] as? String ?: "",
                            profesor = bloqueData["profesor"] as? String ?: "",
                            sala = bloqueData["sala"] as? String ?: ""
                        )
                    }
                    
                    horarioSemanal[diaSemana] = HorarioDia(
                        diaSemana = diaSemana,
                        horaEntrada = diaData["horaEntrada"] as? String ?: "08:00",
                        horaSalida = diaData["horaSalida"] as? String ?: "15:30",
                        bloques = bloques
                    )
                } catch (e: Exception) {
                    Log.e("HorarioRepo", "Error parseando día: ${e.message}")
                }
            }
            
            // Si no hay datos, usar predeterminados
            if (horarioSemanal.isEmpty()) {
                val curso = data["curso"] as? String ?: ""
                HorariosPredeterminados.obtenerHorarioPredeterminado(curso).forEach { (dia, horario) ->
                    horarioSemanal[dia] = horario
                }
            }
            
            HorarioEscolar(
                id = docId,
                curso = data["curso"] as? String ?: "",
                anioEscolar = (data["anioEscolar"] as? Long)?.toInt() ?: 2026,
                horaEntradaGeneral = data["horaEntradaGeneral"] as? String ?: "08:00",
                horarioSemanal = horarioSemanal,
                activo = data["activo"] as? Boolean ?: true,
                fechaCreacion = data["fechaCreacion"] as? Long ?: System.currentTimeMillis(),
                fechaModificacion = data["fechaModificacion"] as? Long ?: System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Log.e("HorarioRepo", "Error parseando horario: ${e.message}")
            null
        }
    }
}
