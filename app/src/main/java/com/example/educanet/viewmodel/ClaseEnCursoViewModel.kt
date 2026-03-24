package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.*
import com.example.educanet.repository.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class ClaseEnCursoUiState(
    val claseActiva: ClaseEnCurso? = null,
    val alumnos: List<AlumnoEnClase> = emptyList(),
    val alumnosConectados: List<AlumnoConectado> = emptyList(),
    val asistenciaMap: Map<String, Boolean> = emptyMap(),
    val calificacionesMap: Map<String, Double> = emptyMap(),
    val materiales: List<MaterialClase> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val tiempoTranscurrido: Long = 0,
    val tiempoRestante: Long = 0,
    val claseIniciada: Boolean = false,
    val claseFinalizada: Boolean = false
)

class ClaseEnCursoViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val notificacionRepository = NotificacionRepository()
    
    private val _uiState = MutableStateFlow(ClaseEnCursoUiState())
    val uiState: StateFlow<ClaseEnCursoUiState> = _uiState.asStateFlow()
    
    // Listener para alumnos conectados en tiempo real
    private var alumnosConectadosListener: com.google.firebase.firestore.ListenerRegistration? = null
    
    /**
     * Inicia una nueva clase y notifica a los alumnos del curso
     */
    fun iniciarClase(asignatura: String, curso: String, profesorNombre: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            
            try {
                val profesorId = auth.currentUser?.uid ?: ""
                val claseId = UUID.randomUUID().toString()
                
                val nuevaClase = ClaseEnCurso(
                    id = claseId,
                    asignatura = asignatura,
                    curso = curso,
                    profesorId = profesorId,
                    profesorNombre = profesorNombre,
                    horaInicio = System.currentTimeMillis(),
                    activa = true,
                    duracionMinutos = HorarioConstantes.DURACION_CLASE_MINUTOS
                )
                
                // Guardar en Firestore
                db.collection("clases_en_curso")
                    .document(claseId)
                    .set(nuevaClase)
                    .await()
                
                // 🔔 Enviar notificación a todos los alumnos del curso
                notificacionRepository.notificarInicioClase(
                    claseId = claseId,
                    asignatura = asignatura,
                    curso = curso,
                    profesorNombre = profesorNombre
                )
                
                // Cargar alumnos del curso (inicialmente todos ausentes hasta que se unan)
                val alumnos = cargarAlumnosCurso(curso)
                val asistenciaInicial = alumnos.associate { it.alumno.id to false } // Todos ausentes por defecto
                val calificacionesInicial = alumnos.associate { it.alumno.id to 0.0 }
                
                // Cargar materiales previos de esta asignatura y curso
                val materiales = cargarMateriales(asignatura, curso)
                
                _uiState.value = _uiState.value.copy(
                    claseActiva = nuevaClase,
                    alumnos = alumnos,
                    asistenciaMap = asistenciaInicial,
                    calificacionesMap = calificacionesInicial,
                    materiales = materiales,
                    claseIniciada = true,
                    cargando = false,
                    mensaje = "Clase iniciada. Se ha notificado a los alumnos."
                )
                
                // 🔴 NUEVO: Iniciar listener de alumnos conectados en tiempo real
                iniciarListenerAlumnosConectados(claseId)
                
                Log.d("ClaseEnCursoVM", "Clase iniciada: $claseId - Notificación enviada al curso $curso")
            } catch (e: Exception) {
                Log.e("ClaseEnCursoVM", "Error al iniciar clase", e)
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensaje = "Error al iniciar la clase: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Carga los alumnos de un curso
     */
    private suspend fun cargarAlumnosCurso(curso: String): List<AlumnoEnClase> {
        return try {
            val snapshot = db.collection("usuario")
                .whereEqualTo("rol", "Alumno")
                .whereEqualTo("curso", curso)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                val alumno = Alumno(
                    id = doc.id,
                    nombre = data["nombre"] as? String ?: "",
                    correo = data["correo"] as? String ?: "",
                    clave = data["clave"] as? String ?: "",
                    rol = data["rol"] as? String ?: "Alumno",
                    fotoUrl = data["fotoUrl"] as? String ?: "",
                    fechaRegistro = data["fechaRegistro"] as? String ?: "",
                    curso = data["curso"] as? String ?: ""
                )
                AlumnoEnClase(alumno = alumno)
            }.sortedBy { it.alumno.nombre }
        } catch (e: Exception) {
            Log.e("ClaseEnCursoVM", "Error cargando alumnos", e)
            emptyList()
        }
    }
    
    /**
     * Carga los materiales de una asignatura y curso
     */
    private suspend fun cargarMateriales(asignatura: String, curso: String): List<MaterialClase> {
        return try {
            val snapshot = db.collection("materiales_clase")
                .whereEqualTo("asignatura", asignatura)
                .whereEqualTo("curso", curso)
                .whereEqualTo("visible", true)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(MaterialClase::class.java)?.copy(id = doc.id)
            }.sortedByDescending { it.fechaSubida }
        } catch (e: Exception) {
            Log.e("ClaseEnCursoVM", "Error cargando materiales", e)
            emptyList()
        }
    }
    
    /**
     * Toggle de asistencia para un alumno
     */
    fun toggleAsistencia(alumnoId: String) {
        val currentMap = _uiState.value.asistenciaMap.toMutableMap()
        currentMap[alumnoId] = !(currentMap[alumnoId] ?: true)
        _uiState.value = _uiState.value.copy(asistenciaMap = currentMap)
    }
    
    /**
     * Marcar todos presentes
     */
    fun marcarTodosPresentes() {
        val nuevaAsistencia = _uiState.value.alumnos.associate { it.alumno.id to true }
        _uiState.value = _uiState.value.copy(asistenciaMap = nuevaAsistencia)
    }
    
    /**
     * Marcar todos ausentes
     */
    fun marcarTodosAusentes() {
        val nuevaAsistencia = _uiState.value.alumnos.associate { it.alumno.id to false }
        _uiState.value = _uiState.value.copy(asistenciaMap = nuevaAsistencia)
    }
    
    /**
     * Actualizar calificación de un alumno
     */
    fun actualizarCalificacion(alumnoId: String, nota: Double) {
        val currentMap = _uiState.value.calificacionesMap.toMutableMap()
        currentMap[alumnoId] = nota.coerceIn(1.0, 7.0)
        _uiState.value = _uiState.value.copy(calificacionesMap = currentMap)
    }
    
    /**
     * Guardar asistencia de la clase
     */
    fun guardarAsistencia() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            try {
                val claseId = _uiState.value.claseActiva?.id ?: return@launch
                val profesorId = auth.currentUser?.uid ?: ""
                val profesorNombre = _uiState.value.claseActiva?.profesorNombre ?: ""
                val asignatura = _uiState.value.claseActiva?.asignatura ?: ""
                val curso = _uiState.value.claseActiva?.curso ?: ""
                
                val batch = db.batch()
                
                _uiState.value.alumnos.forEach { alumnoEnClase ->
                    val asistencia = Asistencia(
                        id = UUID.randomUUID().toString(),
                        profesorId = profesorId,
                        profesorNombre = profesorNombre,
                        alumnoId = alumnoEnClase.alumno.id,
                        alumnoNombre = alumnoEnClase.alumno.nombre,
                        curso = curso,
                        asignaturaId = claseId,
                        asignaturaNombre = asignatura,
                        fecha = System.currentTimeMillis(),
                        presente = _uiState.value.asistenciaMap[alumnoEnClase.alumno.id] ?: true
                    )
                    
                    val docRef = db.collection("asistencia").document(asistencia.id)
                    batch.set(docRef, asistencia)
                }
                
                batch.commit().await()
                
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Asistencia guardada correctamente"
                )
            } catch (e: Exception) {
                Log.e("ClaseEnCursoVM", "Error guardando asistencia", e)
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Error al guardar asistencia: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Guardar calificaciones de la clase
     */
    fun guardarCalificaciones(tipo: TipoCalificacion, descripcion: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            try {
                val claseId = _uiState.value.claseActiva?.id ?: return@launch
                val profesorCorreo = auth.currentUser?.email ?: ""
                val profesorNombre = _uiState.value.claseActiva?.profesorNombre ?: ""
                val asignatura = _uiState.value.claseActiva?.asignatura ?: ""
                val curso = _uiState.value.claseActiva?.curso ?: ""
                
                val batch = db.batch()
                
                _uiState.value.alumnos.forEach { alumnoEnClase ->
                    val nota = _uiState.value.calificacionesMap[alumnoEnClase.alumno.id] ?: 0.0
                    
                    if (nota > 0) {
                        // Guardar en calificaciones_clase para historial
                        val calificacionClase = CalificacionClase(
                            id = UUID.randomUUID().toString(),
                            claseId = claseId,
                            alumnoId = alumnoEnClase.alumno.id,
                            alumnoNombre = alumnoEnClase.alumno.nombre,
                            nota = nota,
                            tipo = tipo,
                            descripcion = descripcion,
                            fecha = System.currentTimeMillis()
                        )
                        
                        val docRef = db.collection("calificaciones_clase").document(calificacionClase.id)
                        batch.set(docRef, calificacionClase)
                        
                        // También guardar en progreso_academico usando los campos del modelo existente
                        val progresoId = UUID.randomUUID().toString()
                        val progreso = ProgresoAcademico(
                            id = progresoId,
                            profesor = profesorNombre,
                            profesorCorreo = profesorCorreo,
                            alumno = alumnoEnClase.alumno.nombre,
                            asignatura = asignatura,
                            curso = curso,
                            notas = nota,
                            fechaCreacion = System.currentTimeMillis(),
                            fechaModificacion = System.currentTimeMillis()
                        )
                        
                        val progresoRef = db.collection("progreso_academico").document(progresoId)
                        batch.set(progresoRef, progreso)
                    }
                }
                
                batch.commit().await()
                
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Calificaciones guardadas correctamente"
                )
            } catch (e: Exception) {
                Log.e("ClaseEnCursoVM", "Error guardando calificaciones", e)
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Error al guardar calificaciones: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Subir material de clase
     */
    fun agregarMaterial(titulo: String, descripcion: String, tipo: TipoMaterial, url: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            try {
                val profesorId = auth.currentUser?.uid ?: ""
                val claseActiva = _uiState.value.claseActiva
                
                val material = MaterialClase(
                    id = UUID.randomUUID().toString(),
                    claseId = claseActiva?.id ?: "",
                    asignatura = claseActiva?.asignatura ?: "",
                    curso = claseActiva?.curso ?: "",
                    titulo = titulo,
                    descripcion = descripcion,
                    tipo = tipo,
                    url = url,
                    nombreArchivo = titulo,
                    profesorId = profesorId,
                    profesorNombre = claseActiva?.profesorNombre ?: "",
                    fechaSubida = System.currentTimeMillis(),
                    visible = true
                )
                
                db.collection("materiales_clase")
                    .document(material.id)
                    .set(material)
                    .await()
                
                // Actualizar lista local
                val materialesActualizados = listOf(material) + _uiState.value.materiales
                
                _uiState.value = _uiState.value.copy(
                    materiales = materialesActualizados,
                    guardando = false,
                    mensaje = "Material agregado correctamente"
                )
            } catch (e: Exception) {
                Log.e("ClaseEnCursoVM", "Error agregando material", e)
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Error al agregar material: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Eliminar material
     */
    fun eliminarMaterial(materialId: String) {
        viewModelScope.launch {
            try {
                db.collection("materiales_clase")
                    .document(materialId)
                    .delete()
                    .await()
                
                val materialesActualizados = _uiState.value.materiales.filter { it.id != materialId }
                
                _uiState.value = _uiState.value.copy(
                    materiales = materialesActualizados,
                    mensaje = "Material eliminado"
                )
            } catch (e: Exception) {
                Log.e("ClaseEnCursoVM", "Error eliminando material", e)
                _uiState.value = _uiState.value.copy(
                    mensaje = "Error al eliminar material: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Finalizar la clase y desactivar notificaciones
     */
    fun finalizarClase(guardarAsistenciaAlFinalizar: Boolean = true): Boolean {
        var resultado = false
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            try {
                val claseId = _uiState.value.claseActiva?.id
                
                if (claseId != null) {
                    // Guardar asistencia si se solicita
                    if (guardarAsistenciaAlFinalizar && _uiState.value.asistenciaMap.isNotEmpty()) {
                        guardarAsistenciaInterno()
                    }
                    
                    // Actualizar clase como finalizada
                    db.collection("clases_en_curso")
                        .document(claseId)
                        .update(
                            mapOf(
                                "activa" to false,
                                "horaFin" to System.currentTimeMillis()
                            )
                        )
                        .await()
                    
                    // � NUEVO: Limpiar alumnos conectados de esta clase
                    limpiarAlumnosConectados(claseId)
                    
                    // �🔔 Desactivar notificaciones de esta clase
                    notificacionRepository.desactivarNotificacionesClase(claseId)
                }
                
                _uiState.value = _uiState.value.copy(
                    claseFinalizada = true,
                    claseIniciada = false,
                    guardando = false,
                    mensaje = "Clase finalizada correctamente"
                )
                resultado = true
            } catch (e: Exception) {
                Log.e("ClaseEnCursoVM", "Error finalizando clase", e)
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Error al finalizar la clase: ${e.message}"
                )
            }
        }
        return resultado
    }
    
    private suspend fun guardarAsistenciaInterno() {
        try {
            val claseId = _uiState.value.claseActiva?.id ?: return
            val profesorId = auth.currentUser?.uid ?: ""
            val profesorNombre = _uiState.value.claseActiva?.profesorNombre ?: ""
            val asignatura = _uiState.value.claseActiva?.asignatura ?: ""
            val curso = _uiState.value.claseActiva?.curso ?: ""
            
            val batch = db.batch()
            
            _uiState.value.alumnos.forEach { alumnoEnClase ->
                val asistencia = Asistencia(
                    id = UUID.randomUUID().toString(),
                    profesorId = profesorId,
                    profesorNombre = profesorNombre,
                    alumnoId = alumnoEnClase.alumno.id,
                    alumnoNombre = alumnoEnClase.alumno.nombre,
                    curso = curso,
                    asignaturaId = claseId,
                    asignaturaNombre = asignatura,
                    fecha = System.currentTimeMillis(),
                    presente = _uiState.value.asistenciaMap[alumnoEnClase.alumno.id] ?: true
                )
                
                val docRef = db.collection("asistencia").document(asistencia.id)
                batch.set(docRef, asistencia)
            }
            
            batch.commit().await()
        } catch (e: Exception) {
            Log.e("ClaseEnCursoVM", "Error guardando asistencia interna", e)
        }
    }
    
    /**
     * Limpiar mensaje
     */
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
    
    /**
     * Actualizar tiempo transcurrido
     */
    fun actualizarTiempo() {
        val claseActiva = _uiState.value.claseActiva ?: return
        val tiempoTranscurrido = (System.currentTimeMillis() - claseActiva.horaInicio) / 1000
        val duracionTotal = claseActiva.duracionMinutos * 60L
        val tiempoRestante = (duracionTotal - tiempoTranscurrido).coerceAtLeast(0)
        
        _uiState.value = _uiState.value.copy(
            tiempoTranscurrido = tiempoTranscurrido,
            tiempoRestante = tiempoRestante
        )
    }
    
    /**
     * Marcar asistencia de un alumno que se une desde la notificación
     */
    fun marcarAsistenciaAlumno(alumnoId: String) {
        val currentMap = _uiState.value.asistenciaMap.toMutableMap()
        currentMap[alumnoId] = true
        _uiState.value = _uiState.value.copy(asistenciaMap = currentMap)
    }
    
    /**
     * 🔴 NUEVO: Iniciar listener en tiempo real para alumnos conectados
     */
    private fun iniciarListenerAlumnosConectados(claseId: String) {
        // Remover listener previo si existe
        alumnosConectadosListener?.remove()
        
        alumnosConectadosListener = db.collection("alumnos_conectados")
            .whereEqualTo("claseId", claseId)
            .whereEqualTo("conectado", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ClaseEnCursoVM", "Error escuchando alumnos conectados", error)
                    return@addSnapshotListener
                }
                
                val alumnosConectados = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        AlumnoConectado(
                            id = doc.id,
                            claseId = doc.getString("claseId") ?: "",
                            alumnoId = doc.getString("alumnoId") ?: "",
                            alumnoNombre = doc.getString("alumnoNombre") ?: "",
                            alumnoCorreo = doc.getString("alumnoCorreo") ?: "",
                            horaConexion = doc.getLong("horaConexion") ?: System.currentTimeMillis(),
                            conectado = doc.getBoolean("conectado") ?: false
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()
                
                // Actualizar mapa de asistencia automáticamente con alumnos conectados
                val nuevaAsistencia = _uiState.value.asistenciaMap.toMutableMap()
                alumnosConectados.forEach { alumno ->
                    nuevaAsistencia[alumno.alumnoId] = true
                }
                
                _uiState.value = _uiState.value.copy(
                    alumnosConectados = alumnosConectados,
                    asistenciaMap = nuevaAsistencia
                )
                
                Log.d("ClaseEnCursoVM", "Alumnos conectados: ${alumnosConectados.size}")
            }
    }
    
    /**
     * 🔴 NUEVO: Limpiar alumnos conectados cuando termina la clase
     */
    private suspend fun limpiarAlumnosConectados(claseId: String) {
        try {
            // Remover listener
            alumnosConectadosListener?.remove()
            alumnosConectadosListener = null
            
            // Eliminar registros de alumnos conectados
            val snapshot = db.collection("alumnos_conectados")
                .whereEqualTo("claseId", claseId)
                .get()
                .await()
            
            val batch = db.batch()
            snapshot.documents.forEach { doc ->
                batch.delete(doc.reference)
            }
            batch.commit().await()
            
            Log.d("ClaseEnCursoVM", "Alumnos conectados limpiados para clase: $claseId")
        } catch (e: Exception) {
            Log.e("ClaseEnCursoVM", "Error limpiando alumnos conectados", e)
        }
    }
    
    /**
     * 🔴 NUEVO: Limpiar recursos cuando el ViewModel se destruye
     */
    override fun onCleared() {
        super.onCleared()
        alumnosConectadosListener?.remove()
    }
}

/**
 * ViewModel para que el alumno pueda unirse a clases activas
 */
class UnirseClaseViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val notificacionRepository = NotificacionRepository()
    
    private val _uiState = MutableStateFlow(UnirseClaseUiState())
    val uiState: StateFlow<UnirseClaseUiState> = _uiState.asStateFlow()
    
    /**
     * Cargar clases activas para el curso del alumno
     */
    fun cargarClasesActivas(curso: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            
            try {
                val snapshot = db.collection("clases_en_curso")
                    .whereEqualTo("curso", curso)
                    .whereEqualTo("activa", true)
                    .get()
                    .await()
                
                val clases = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(ClaseEnCurso::class.java)?.copy(id = doc.id)
                }
                
                _uiState.value = _uiState.value.copy(
                    clasesActivas = clases,
                    cargando = false
                )
            } catch (e: Exception) {
                Log.e("UnirseClaseVM", "Error cargando clases activas", e)
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    mensaje = "Error al cargar clases: ${e.message}"
                )
            }
        }
    }
    
    /**
     * Unirse a una clase y marcar asistencia
     */
    fun unirseAClase(claseId: String, alumnoId: String, alumnoNombre: String, notificacionId: String = "") {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(uniendose = true)
            
            try {
                // Registrar la asistencia del alumno
                val asistenciaId = UUID.randomUUID().toString()
                val clase = _uiState.value.clasesActivas.find { it.id == claseId }
                
                if (clase != null) {
                    val alumnoCorreo = auth.currentUser?.email ?: ""
                    
                    val asistencia = Asistencia(
                        id = asistenciaId,
                        profesorId = clase.profesorId,
                        profesorNombre = clase.profesorNombre,
                        alumnoId = alumnoId,
                        alumnoNombre = alumnoNombre,
                        curso = clase.curso,
                        asignaturaId = clase.id,
                        asignaturaNombre = clase.asignatura,
                        fecha = System.currentTimeMillis(),
                        presente = true,
                        justificacion = ""
                    )
                    
                    db.collection("asistencia")
                        .document(asistenciaId)
                        .set(asistencia)
                        .await()
                    
                    // 🔴 NUEVO: Registrar alumno conectado en tiempo real
                    val alumnoConectado = AlumnoConectado(
                        id = "${claseId}_${alumnoId}",
                        claseId = claseId,
                        alumnoId = alumnoId,
                        alumnoNombre = alumnoNombre,
                        alumnoCorreo = alumnoCorreo,
                        horaConexion = System.currentTimeMillis(),
                        conectado = true
                    )
                    
                    db.collection("alumnos_conectados")
                        .document(alumnoConectado.id)
                        .set(alumnoConectado)
                        .await()
                    
                    // Marcar la notificación como realizada
                    if (notificacionId.isNotEmpty()) {
                        notificacionRepository.marcarUnionAClase(notificacionId)
                    }
                    
                    _uiState.value = _uiState.value.copy(
                        uniendose = false,
                        unidoExitosamente = true,
                        mensaje = "¡Te has unido a la clase de ${clase.asignatura}! Tu asistencia ha sido registrada."
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        uniendose = false,
                        mensaje = "La clase ya no está disponible"
                    )
                }
            } catch (e: Exception) {
                Log.e("UnirseClaseVM", "Error uniéndose a clase", e)
                _uiState.value = _uiState.value.copy(
                    uniendose = false,
                    mensaje = "Error al unirse: ${e.message}"
                )
            }
        }
    }
    
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
    
    fun resetearEstado() {
        _uiState.value = _uiState.value.copy(unidoExitosamente = false)
    }
}

data class UnirseClaseUiState(
    val clasesActivas: List<ClaseEnCurso> = emptyList(),
    val cargando: Boolean = false,
    val uniendose: Boolean = false,
    val unidoExitosamente: Boolean = false,
    val mensaje: String? = null
)
