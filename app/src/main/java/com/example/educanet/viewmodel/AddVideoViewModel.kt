package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Profesor
import com.example.educanet.model.VideoApoyo
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.repository.VideoApoyoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AddVideoUiState(
    val nombre: String = "",
    val nivel: String = "",
    val videoUrl: String = "",
    val descripcion: String = "",
    val duracion: Int = 0,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddVideoViewModel : ViewModel() {

    private val videoApoyoRepository = VideoApoyoRepository()
    private val notificacionRepository = NotificacionRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AddVideoUiState())
    val uiState: StateFlow<AddVideoUiState> = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre)
    }

    fun onNivelChange(nivel: String) {
        _uiState.value = _uiState.value.copy(nivel = nivel)
    }

    fun onVideoUrlChange(videoUrl: String) {
        _uiState.value = _uiState.value.copy(videoUrl = videoUrl)
    }

    fun onDescripcionChange(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcion = descripcion)
    }
    fun onDuracionChange(duracion: String) {
        val duracionInt = duracion.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(duracion = duracionInt)
    }

    fun saveVideo() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Usuario no autenticado.", isSaving = false)
                    return@launch
                }

                val userDoc = db.collection("users").document(currentUser.uid).get().await()
                val userRole = userDoc.getString("rol")

                if (userRole != "Profesor") {
                    _uiState.value = _uiState.value.copy(errorMessage = "Solo los profesores pueden agregar videos.", isSaving = false)
                    return@launch
                }
                
                val profesor = userDoc.toObject(Profesor::class.java)
                if (profesor == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudieron obtener los datos del profesor.", isSaving = false)
                    return@launch
                }

                val video = VideoApoyo(
                    nombre = _uiState.value.nombre,
                    nivel = _uiState.value.nivel,
                    video = _uiState.value.videoUrl,
                    descripcion = _uiState.value.descripcion,
                    duracion = _uiState.value.duracion,
                    profesor = profesor
                )

                val success = videoApoyoRepository.agregarVideo(video)

                if (success) {
                    notificacionRepository.agregarNotificacion(
                        titulo = "Nuevo video de apoyo agregado",
                        mensaje = "Se ha agregado el video: ${_uiState.value.nombre}"
                    )
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Error al guardar el video.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message ?: "Ocurrió un error desconocido.")
            }
        }
    }
}
