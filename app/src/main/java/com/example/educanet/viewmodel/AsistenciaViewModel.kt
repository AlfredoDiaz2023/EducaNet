package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Alumno
import com.example.educanet.model.Asignatura
import com.example.educanet.model.Asistencia
import com.example.educanet.model.Cursos
import com.example.educanet.repository.AsignaturaRepository
import com.example.educanet.repository.AsistenciaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AsistenciaUiState(
    val cursoSeleccionado: String = "",
    val asignaturaSeleccionada: Asignatura? = null,
    val asignaturasPorCurso: List<Asignatura> = emptyList(),
    val alumnos: List<Alumno> = emptyList(),
    val asistenciaMap: Map<String, Boolean> = emptyMap(), // alumnoId -> presente
    val justificacionMap: Map<String, String> = emptyMap(), // alumnoId -> justificación
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val yaSeTomoAsistencia: Boolean = false,
    val historialAsistencia: List<Asistencia> = emptyList(),
    val porcentajeAsistencia: Double = 0.0
)

class AsistenciaViewModel : ViewModel() {
    private val repository = AsistenciaRepository()
    private val asignaturaRepository = AsignaturaRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AsistenciaUiState())
    val uiState: StateFlow<AsistenciaUiState> = _uiState.asStateFlow()

    val cursos = Cursos.lista

    fun seleccionarCurso(curso: String) {
        _uiState.value = _uiState.value.copy(cursoSeleccionado = curso, cargando = true)
        viewModelScope.launch {
            // Cargar asignaturas del curso
            val asignaturas = asignaturaRepository.obtenerAsignaturasPorCurso(curso)
            
            // Cargar alumnos del curso
            val alumnos = repository.obtenerAlumnosPorCurso(curso)
            
            // Inicializar mapa de asistencia (todos presentes por defecto)
            val asistenciaInicial = alumnos.associate { it.id to true }
            val justificacionInicial = alumnos.associate { it.id to "" }

            _uiState.value = _uiState.value.copy(
                alumnos = alumnos,
                asignaturasPorCurso = asignaturas,
                asistenciaMap = asistenciaInicial,
                justificacionMap = justificacionInicial,
                cargando = false,
                yaSeTomoAsistencia = false
            )
        }
    }

    fun seleccionarAsignatura(asignatura: Asignatura) {
        _uiState.value = _uiState.value.copy(asignaturaSeleccionada = asignatura)
        viewModelScope.launch {
            // Verificar si ya se tomó asistencia hoy para este curso y asignatura
            val yaTomoAsistencia = repository.yaSeTomoAsistenciaHoy(
                _uiState.value.cursoSeleccionado,
                asignatura.id
            )
            _uiState.value = _uiState.value.copy(yaSeTomoAsistencia = yaTomoAsistencia)
        }
    }

    fun toggleAsistencia(alumnoId: String) {
        val currentMap = _uiState.value.asistenciaMap.toMutableMap()
        currentMap[alumnoId] = !(currentMap[alumnoId] ?: true)
        _uiState.value = _uiState.value.copy(asistenciaMap = currentMap)
    }

    fun setJustificacion(alumnoId: String, justificacion: String) {
        val currentMap = _uiState.value.justificacionMap.toMutableMap()
        currentMap[alumnoId] = justificacion
        _uiState.value = _uiState.value.copy(justificacionMap = currentMap)
    }

    fun marcarTodosPresentes() {
        val nuevaAsistencia = _uiState.value.alumnos.associate { it.id to true }
        _uiState.value = _uiState.value.copy(asistenciaMap = nuevaAsistencia)
    }

    fun marcarTodosAusentes() {
        val nuevaAsistencia = _uiState.value.alumnos.associate { it.id to false }
        _uiState.value = _uiState.value.copy(asistenciaMap = nuevaAsistencia)
    }

    fun guardarAsistencia(profesorNombre: String) {
        val asignatura = _uiState.value.asignaturaSeleccionada
        if (asignatura == null) {
            _uiState.value = _uiState.value.copy(mensaje = "❌ Selecciona una asignatura primero")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)

            try {
                val profesorId = auth.currentUser?.uid ?: ""
                val curso = _uiState.value.cursoSeleccionado
                val fechaActual = System.currentTimeMillis()
                
                Log.d("AsistenciaVM", "Guardando asistencia - profesorId: $profesorId, profesorNombre: $profesorNombre, curso: $curso, asignatura: ${asignatura.nombre}")
                Log.d("AsistenciaVM", "Cantidad de alumnos: ${_uiState.value.alumnos.size}")

                val listaAsistencia = _uiState.value.alumnos.map { alumno ->
                    Asistencia(
                        profesorId = profesorId,
                        profesorNombre = profesorNombre,
                        alumnoId = alumno.id,
                        alumnoNombre = alumno.nombre,
                        curso = curso,
                        asignaturaId = asignatura.id,
                        asignaturaNombre = asignatura.nombre,
                        fecha = fechaActual,
                        presente = _uiState.value.asistenciaMap[alumno.id] ?: true,
                        justificacion = _uiState.value.justificacionMap[alumno.id] ?: ""
                    )
                }

                val exito = repository.registrarAsistenciaMasiva(listaAsistencia)
                Log.d("AsistenciaVM", "Resultado de guardar: $exito")

                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = if (exito) "✅ Asistencia guardada correctamente" else "❌ Error al guardar la asistencia",
                    yaSeTomoAsistencia = exito
                )
            } catch (e: Exception) {
                Log.e("AsistenciaVM", "Error guardando asistencia: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "❌ Error: ${e.message}"
                )
            }
        }
    }

    fun cargarHistorialProfesor() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            val profesorId = auth.currentUser?.uid ?: ""
            Log.d("AsistenciaVM", "Cargando historial para profesorId (uid): $profesorId")
            val historial = repository.obtenerHistorialProfesor(profesorId)
            Log.d("AsistenciaVM", "Historial cargado: ${historial.size} registros")
            _uiState.value = _uiState.value.copy(
                historialAsistencia = historial,
                cargando = false
            )
        }
    }

    fun cargarAsistenciaAlumno(alumnoId: String) {
        viewModelScope.launch {
            Log.d("AsistenciaVM", "Cargando asistencia para alumnoId: $alumnoId")
            _uiState.value = _uiState.value.copy(cargando = true)
            val historial = repository.obtenerAsistenciaDeAlumno(alumnoId)
            Log.d("AsistenciaVM", "Historial obtenido: ${historial.size} registros")
            val porcentaje = repository.calcularPorcentajeAsistencia(alumnoId)
            Log.d("AsistenciaVM", "Porcentaje de asistencia: $porcentaje%")
            _uiState.value = _uiState.value.copy(
                historialAsistencia = historial,
                porcentajeAsistencia = porcentaje,
                cargando = false
            )
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
    
    fun refreshHistorialProfesor() {
        cargarHistorialProfesor()
    }
    
    fun refreshAsistenciaAlumno(alumnoId: String) {
        cargarAsistenciaAlumno(alumnoId)
    }

    fun resetEstado() {
        _uiState.value = AsistenciaUiState()
    }
}
