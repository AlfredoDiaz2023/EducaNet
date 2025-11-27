package com.example.educanet.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Alumno // Importa tu modelo Alumno
import com.example.educanet.repository.AlumnoRepository
import com.example.educanet.repository.PhotoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class PerfilUiState(
    val nombre: String = "Cargando...",
    val fotoUrl: String? = null,
    val isUploading: Boolean = false,
    val errorMessage: String? = null
)

class PerfilViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val alumnoRepository = AlumnoRepository()
    private val photoRepository = PhotoRepository() // Usar el nuevo repositorio

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
    }

    private fun loadUserData() {
        viewModelScope.launch {
            val userEmail = auth.currentUser?.email
            if (userEmail != null) {
                // Asumimos que obtendremos el perfil del alumno por email
                val alumno = alumnoRepository.obtenerAlumnoPorEmail(userEmail)

                if (alumno != null) {
                    _uiState.value = _uiState.value.copy(
                        nombre = alumno.nombre,
                        fotoUrl = alumno.fotoUrl,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(errorMessage = "Datos de usuario no encontrados.")
                }
            } else {
                _uiState.value = _uiState.value.copy(errorMessage = "Usuario no autenticado.")
            }
        }
    }

    fun onImageSelectedAndSave(context: Context, uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploading = true, errorMessage = null)
            val uid = auth.currentUser?.uid
            val userEmail = auth.currentUser?.email

            if (uid == null || userEmail == null) {
                _uiState.value = _uiState.value.copy(isUploading = false, errorMessage = "Error de autenticación.")
                return@launch
            }

            try {
                // 1. Subir la imagen a Firebase Storage
                val path = "perfiles/$uid/profile_${UUID.randomUUID()}.jpg"
                val urlImagen = photoRepository.uploadImage(context, uri, path)

                // 2. Obtener el Document ID del alumno
                val alumno = alumnoRepository.obtenerAlumnoPorEmail(userEmail)
                val documentId = alumno?.id // Necesitas el ID de Firestore

                if (documentId == null) {
                    throw Exception("No se pudo encontrar el documento del alumno para actualizar.")
                }

                // 3. Guardar la URL pública en el documento de Firestore
                val success = alumnoRepository.actualizarFotoPerfil(documentId, urlImagen)

                if (success) {
                    // Actualizar el estado con la nueva URL permanente
                    _uiState.value = _uiState.value.copy(
                        fotoUrl = urlImagen,
                        isUploading = false,
                        errorMessage = null
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isUploading = false,
                        errorMessage = "Error al guardar URL en Firestore."
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isUploading = false,
                    errorMessage = "Error al subir la foto: ${e.message}"
                )
            }
        }
    }
}