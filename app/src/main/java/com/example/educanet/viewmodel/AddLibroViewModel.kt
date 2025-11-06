package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Libro
import com.example.educanet.repository.LibroRepository
import com.example.educanet.repository.NotificacionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddLibroUiState(
    val nombre: String = "",
    val nivel: String = "",
    val cantidad: Int = 0,
    val imagen: String = "",
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
        val cantidadInt = cantidad.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(cantidad = cantidadInt)
    }

    fun onImagenChange(imagen: String) {
        _uiState.value = _uiState.value.copy(imagen = imagen)
    }

    fun saveLibro() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val libro = Libro(
                    nombre = _uiState.value.nombre,
                    nivel = _uiState.value.nivel,
                    cantidad = _uiState.value.cantidad,
                    imagen = _uiState.value.imagen
                )

                val success = libroRepository.agregarLibro(libro)

                if (success) {
                    notificacionRepository.agregarNotificacion(
                        titulo = "Nuevo libro agregado",
                        mensaje = "Se ha agregado el libro: ${_uiState.value.nombre}"
                    )
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Error al guardar el libro")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message)
            }
        }
    }
}
