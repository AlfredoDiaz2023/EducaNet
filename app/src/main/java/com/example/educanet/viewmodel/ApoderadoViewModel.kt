package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Alumno
import com.example.educanet.model.ProgresoAcademico
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ApoderadoUiState(
    val nombreApoderado: String = "",
    val fotoUrlApoderado: String = "",
    val alumnoVinculado: AlumnoVinculadoInfo? = null,
    val notasAlumno: List<ProgresoAcademico> = emptyList(),
    val cargando: Boolean = false,
    val errorMessage: String? = null
)

data class AlumnoVinculadoInfo(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val fotoUrl: String = ""
)

class ApoderadoViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ApoderadoUiState())
    val uiState: StateFlow<ApoderadoUiState> = _uiState

    fun cargarDatosApoderado() {
        val uid = auth.currentUser?.uid ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                // Buscar datos del apoderado
                val apoderadoSnapshot = db.collection("usuario")
                    .whereEqualTo("uid", uid)
                    .get()
                    .await()

                if (apoderadoSnapshot.isEmpty) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorMessage = "No se encontró el apoderado"
                    )
                    return@launch
                }

                val apoderadoDoc = apoderadoSnapshot.documents.first()
                val nombreApoderado = apoderadoDoc.getString("nombre") ?: ""
                val fotoUrlApoderado = apoderadoDoc.getString("fotoUrl") ?: ""
                val alumnoVinculadoId = apoderadoDoc.getString("alumnoVinculadoId")
                val alumnoVinculadoNombre = apoderadoDoc.getString("alumnoVinculadoNombre")
                val alumnoVinculadoCorreo = apoderadoDoc.getString("alumnoVinculadoCorreo")

                Log.d("ApoderadoVM", "Apoderado: $nombreApoderado")
                Log.d("ApoderadoVM", "Alumno vinculado ID: $alumnoVinculadoId")
                Log.d("ApoderadoVM", "Alumno vinculado Correo: $alumnoVinculadoCorreo")

                _uiState.value = _uiState.value.copy(
                    nombreApoderado = nombreApoderado,
                    fotoUrlApoderado = fotoUrlApoderado
                )

                // Si hay alumno vinculado, cargar su información y notas
                if (!alumnoVinculadoId.isNullOrEmpty() && !alumnoVinculadoCorreo.isNullOrEmpty()) {
                    val alumnoInfo = AlumnoVinculadoInfo(
                        id = alumnoVinculadoId,
                        nombre = alumnoVinculadoNombre ?: "",
                        correo = alumnoVinculadoCorreo
                    )
                    _uiState.value = _uiState.value.copy(alumnoVinculado = alumnoInfo)

                    // Cargar notas del alumno
                    cargarNotasAlumno(alumnoVinculadoCorreo)
                } else {
                    _uiState.value = _uiState.value.copy(cargando = false)
                }

            } catch (e: Exception) {
                Log.e("ApoderadoVM", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    errorMessage = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    private suspend fun cargarNotasAlumno(correoAlumno: String) {
        try {
            Log.d("ApoderadoVM", "Cargando notas para: $correoAlumno")
            
            val notasSnapshot = db.collection("progreso_academico")
                .whereEqualTo("alumno", correoAlumno)
                .get()
                .await()

            val notas = notasSnapshot.documents.mapNotNull { doc ->
                try {
                    ProgresoAcademico(
                        id = doc.id,
                        alumno = doc.getString("alumno") ?: "",
                        asignatura = doc.getString("asignatura") ?: "",
                        notas = doc.getDouble("notas") ?: 0.0,
                        profesor = doc.getString("profesor") ?: "",
                        curso = doc.getString("curso") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Log.d("ApoderadoVM", "Notas cargadas: ${notas.size}")
            
            _uiState.value = _uiState.value.copy(
                notasAlumno = notas,
                cargando = false
            )
        } catch (e: Exception) {
            Log.e("ApoderadoVM", "Error cargando notas: ${e.message}")
            _uiState.value = _uiState.value.copy(
                cargando = false,
                errorMessage = "Error al cargar notas: ${e.message}"
            )
        }
    }

    fun refreshData() {
        cargarDatosApoderado()
    }
}
