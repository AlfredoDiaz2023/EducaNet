package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.repository.ProgresoAcademicoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddProgresoAcademicoUiState(
    val profesor: String = "",
    val alumno: String = "",
    val asignatura: String = "",
    val curso: String = "",
    val notas: String = "",
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddProgresoAcademicoViewModel : ViewModel() {
    private val repo = ProgresoAcademicoRepository()
    private val notificacionRepository = NotificacionRepository()

    private val _uiState = MutableStateFlow(AddProgresoAcademicoUiState())
    val uiState: StateFlow<AddProgresoAcademicoUiState> = _uiState.asStateFlow()

    fun onProfesorChange(value: String) = update { copy(profesor = value) }
    fun onAlumnoChange(value: String) = update { copy(alumno = value) }
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
                    alumno = _uiState.value.alumno,
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
