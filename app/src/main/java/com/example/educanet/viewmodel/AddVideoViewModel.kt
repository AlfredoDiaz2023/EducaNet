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
import java.util.regex.Pattern

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

    private fun getYouTubeId(youTubeUrl: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed\\?feature=oembed&url=http%3A%2F%2Fwww.youtube.com%2Fwatch%3Fv%3D|%2Fv%2F|e(?:mbed)?%2F|watch%3Fv%3D|v%2F|%3Fv%3D)[^#&?]*.{11}"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(youTubeUrl)
        return if (matcher.find()) {
            matcher.group()
        } else {
            null
        }
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
                if (!userDoc.exists()) {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se encontró el documento del usuario.", isSaving = false)
                    return@launch
                }

                val userRole = userDoc.getString("rol")
                if (userRole?.equals("Profesor", ignoreCase = true) != true) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Solo los profesores pueden agregar videos.", isSaving = false)
                    return@launch
                }
                
                val profesorNombre = userDoc.getString("nombre")
                if (profesorNombre == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "El documento del profesor no tiene nombre.", isSaving = false)
                    return@launch
                }
                val profesor = Profesor(nombre = profesorNombre, correo = currentUser.email ?: "")

                val videoId = getYouTubeId(_uiState.value.videoUrl)
                if (videoId == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "La URL de YouTube no es válida.", isSaving = false)
                    return@launch
                }

                val embedUrl = "https://www.youtube.com/embed/$videoId"

                val video = VideoApoyo(
                    nombre = _uiState.value.nombre,
                    nivel = _uiState.value.nivel,
                    video = embedUrl, // Guardamos la URL de incrustación
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
