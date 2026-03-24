package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.model.SistemaNotas
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.repository.ProgresoAcademicoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AlumnoInfo(
    val nombre: String = "",
    val correo: String = "",
    val curso: String = ""
)

data class AddProgresoAcademicoUiState(
    val profesor: String = "",
    val alumno: String = "",
    val alumnoCorreo: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val notas: String = "",
    val porcentaje: String = "",
    val notaCalculada: Double = 0.0,
    val modoIngreso: ModoIngresoNota = ModoIngresoNota.PORCENTAJE,
    val puntajeObtenido: String = "",
    val puntajeTotal: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val alumnos: List<AlumnoInfo> = emptyList(),
    val isLoadingAlumnos: Boolean = false,
    val isLoadingProfesor: Boolean = false
)

enum class ModoIngresoNota {
    PORCENTAJE,    // Ingresar porcentaje directo
    PUNTAJE,       // Ingresar puntaje obtenido / puntaje total
    NOTA_DIRECTA   // Ingresar nota directamente
}

class AddProgresoAcademicoViewModel : ViewModel() {
    private val repo = ProgresoAcademicoRepository()
    private val notificacionRepository = NotificacionRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(AddProgresoAcademicoUiState())
    val uiState: StateFlow<AddProgresoAcademicoUiState> = _uiState.asStateFlow()

    init {
        cargarNombreProfesor()
        cargarAlumnos()
    }

    private fun cargarNombreProfesor() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingProfesor = true)
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val snapshot = db.collection("usuario")
                        .whereEqualTo("correo", currentUser.email)
                        .get()
                        .await()
                    
                    val nombre = snapshot.documents.firstOrNull()?.getString("nombre") ?: ""
                    _uiState.value = _uiState.value.copy(
                        profesor = nombre,
                        isLoadingProfesor = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoadingProfesor = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingProfesor = false,
                    errorMessage = "Error al cargar datos del profesor: ${e.message}"
                )
            }
        }
    }

    private fun cargarAlumnos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingAlumnos = true)
            try {
                val snapshot = db.collection("usuario")
                    .whereEqualTo("rol", "Alumno")
                    .get()
                    .await()
                
                val listaAlumnos = snapshot.documents.mapNotNull { doc ->
                    val nombre = doc.getString("nombre") ?: return@mapNotNull null
                    val correo = doc.getString("correo") ?: return@mapNotNull null
                    val curso = doc.getString("curso") ?: ""
                    AlumnoInfo(nombre = nombre, correo = correo, curso = curso)
                }.sortedBy { it.nombre }
                
                _uiState.value = _uiState.value.copy(
                    alumnos = listaAlumnos,
                    isLoadingAlumnos = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingAlumnos = false,
                    errorMessage = "Error al cargar alumnos: ${e.message}"
                )
            }
        }
    }

    fun onProfesorChange(value: String) = update { copy(profesor = value) }
    
    fun onAlumnoSelected(alumno: AlumnoInfo) {
        _uiState.value = _uiState.value.copy(
            alumno = alumno.nombre,
            alumnoCorreo = alumno.correo,
            curso = alumno.curso // Auto-seleccionar el curso del alumno
        )
    }
    fun onAsignaturaChange(value: String) = update { copy(asignatura = value) }
    fun onCursoChange(value: String) = update { copy(curso = value) }
    fun onNotasChange(value: String) {
        val nota = value.toDoubleOrNull()
        if (nota != null) {
            val notaValidada = nota.coerceIn(SistemaNotas.NOTA_MINIMA, SistemaNotas.NOTA_MAXIMA)
            _uiState.value = _uiState.value.copy(
                notas = value,
                notaCalculada = notaValidada
            )
        } else {
            _uiState.value = _uiState.value.copy(notas = value)
        }
    }
    
    fun onPorcentajeChange(value: String) {
        val porcentaje = value.toDoubleOrNull()
        if (porcentaje != null) {
            val porcentajeValidado = porcentaje.coerceIn(0.0, 100.0)
            val notaCalculada = SistemaNotas.porcentajeANota(porcentajeValidado)
            _uiState.value = _uiState.value.copy(
                porcentaje = value,
                notaCalculada = notaCalculada,
                notas = String.format("%.1f", notaCalculada)
            )
        } else {
            _uiState.value = _uiState.value.copy(porcentaje = value)
        }
    }
    
    fun onPuntajeObtenidoChange(value: String) {
        _uiState.value = _uiState.value.copy(puntajeObtenido = value)
        calcularNotaDesdePuntaje()
    }
    
    fun onPuntajeTotalChange(value: String) {
        _uiState.value = _uiState.value.copy(puntajeTotal = value)
        calcularNotaDesdePuntaje()
    }
    
    private fun calcularNotaDesdePuntaje() {
        val obtenido = _uiState.value.puntajeObtenido.toDoubleOrNull() ?: return
        val total = _uiState.value.puntajeTotal.toDoubleOrNull() ?: return
        if (total > 0) {
            val porcentaje = (obtenido / total) * 100.0
            val notaCalculada = SistemaNotas.porcentajeANota(porcentaje)
            _uiState.value = _uiState.value.copy(
                porcentaje = String.format("%.1f", porcentaje),
                notaCalculada = notaCalculada,
                notas = String.format("%.1f", notaCalculada)
            )
        }
    }
    
    fun onModoIngresoChange(modo: ModoIngresoNota) {
        _uiState.value = _uiState.value.copy(
            modoIngreso = modo,
            // Limpiar valores al cambiar de modo
            porcentaje = "",
            puntajeObtenido = "",
            puntajeTotal = "",
            notas = "",
            notaCalculada = 0.0
        )
    }
    
    fun getDescripcionNota(): String {
        return SistemaNotas.getDescripcion(_uiState.value.notaCalculada)
    }
    
    fun esNotaAprobatoria(): Boolean {
        return SistemaNotas.esAprobado(_uiState.value.notaCalculada)
    }

    private fun update(block: AddProgresoAcademicoUiState.() -> AddProgresoAcademicoUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun saveNota() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                // Validar que la nota esté en rango válido
                val nota = _uiState.value.notaCalculada
                if (nota < SistemaNotas.NOTA_MINIMA || nota > SistemaNotas.NOTA_MAXIMA) {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "La nota debe estar entre ${SistemaNotas.NOTA_MINIMA} y ${SistemaNotas.NOTA_MAXIMA}"
                    )
                    return@launch
                }
                
                // Obtener correo del profesor actual
                val correoProfesor = auth.currentUser?.email ?: ""
                
                val data = ProgresoAcademico(
                    profesor = _uiState.value.profesor,
                    profesorCorreo = correoProfesor,
                    alumno = _uiState.value.alumnoCorreo.ifEmpty { _uiState.value.alumno },
                    asignatura = _uiState.value.asignatura,
                    curso = _uiState.value.curso,
                    notas = nota,
                    fechaCreacion = System.currentTimeMillis(),
                    fechaModificacion = System.currentTimeMillis()
                )

                val ok = repo.agregarNota(data)
                if (ok) {
                    val descripcion = SistemaNotas.getDescripcion(nota)
                    val estado = if (SistemaNotas.esAprobado(nota)) "Aprobado" else "Reprobado"
                    notificacionRepository.agregarNotificacion(
                        titulo = "Nueva nota agregada",
                        mensaje = "Nota: ${String.format("%.1f", nota)} ($descripcion - $estado) para ${_uiState.value.alumno} en ${_uiState.value.asignatura}."
                    )
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = true
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        saveSuccess = false,
                        errorMessage = "Error al guardar nota"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    errorMessage = e.message
                )
            }
        }
    }
}