package com.example.educanet.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Libro
import com.example.educanet.repository.LibroRepository
import com.example.educanet.repository.NotificacionRepository
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AddLibroUiState(
    val nombre: String = "",
    val nivel: String = "",
    val cantidad: Int = 0,
    val imagenUri: Uri? = null,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddLibroViewModel : ViewModel() {

    private val libroRepository = LibroRepository()
    private val notificacionRepository = NotificacionRepository()

    private val _uiState = MutableStateFlow(AddLibroUiState())
    val uiState: StateFlow<AddLibroUiState> = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre)
    }

    fun onNivelChange(nivel: String) {
        _uiState.value = _uiState.value.copy(nivel = nivel)
    }

    fun onCantidadChange(cantidad: String) {
        _uiState.value = _uiState.value.copy(cantidad = cantidad.toIntOrNull() ?: 0)
    }

    fun onImagenChange(uri: Uri?) {
        _uiState.value = _uiState.value.copy(imagenUri = uri)
    }

    private suspend fun subirImagen(uri: Uri): String {
        val storageRef = FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("libros/${System.currentTimeMillis()}.jpg")

        return suspendCancellableCoroutine { continuation ->
            fileRef.putFile(uri)
                .addOnSuccessListener {
                    fileRef.downloadUrl.addOnSuccessListener { url ->
                        continuation.resume(url.toString())
                    }
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }

    fun saveLibro() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val uri = _uiState.value.imagenUri
                    ?: throw Exception("Debe seleccionar una imagen")

                val url = subirImagen(uri)

                val libro = Libro(
                    nombre = _uiState.value.nombre,
                    nivel = _uiState.value.nivel,
                    cantidad = _uiState.value.cantidad,
                    imagen = url
                )

                val success = libroRepository.agregarLibro(libro)

                if (success) {
                    notificacionRepository.agregarNotificacion(
                        titulo = "Nuevo libro agregado",
                        mensaje = "Se agregó: ${_uiState.value.nombre}"
                    )
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        errorMessage = "Error al guardar el libro"
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
