package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ProgresoAcademico
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
    val correo: String = ""
)

data class AddProgresoAcademicoUiState(
    val profesor: String = "",
    val alumno: String = "",
    val alumnoCorreo: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val notas: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null,
    val alumnos: List<AlumnoInfo> = emptyList(),
    val isLoadingAlumnos: Boolean = false,
    val isLoadingProfesor: Boolean = false
)

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
                    AlumnoInfo(nombre = nombre, correo = correo)
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
            alumnoCorreo = alumno.correo
        )
    }
    fun onAsignaturaChange(value: String) = update { copy(asignatura = value) }
    fun onCursoChange(value: String) = update { copy(curso = value) }
    fun onNotasChange(value: String) = update { copy(notas = value) }

    private fun update(block: AddProgresoAcademicoUiState.() -> AddProgresoAcademicoUiState) {
        _uiState.value = _uiState.value.block()
    }

    fun saveNota() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val nota = _uiState.value.notas.toDoubleOrNull() ?: 0.0
                val data = ProgresoAcademico(
                    profesor = _uiState.value.profesor,
                    alumno = _uiState.value.alumnoCorreo.ifEmpty { _uiState.value.alumno },
                    asignatura = _uiState.value.asignatura,
                    curso = _uiState.value.curso,
                    notas = nota
                )

                val ok = repo.agregarNota(data)
                if (ok) {
                    notificacionRepository.agregarNotificacion(
                        titulo = "Nueva nota agregada",
                        mensaje = "Se agregó una nota de ${_uiState.value.notas} para el alumno ${_uiState.value.alumno} en la asignatura ${_uiState.value.asignatura}."
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
