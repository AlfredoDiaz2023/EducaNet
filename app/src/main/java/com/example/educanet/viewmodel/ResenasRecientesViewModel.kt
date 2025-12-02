package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.repository.ResenaConLibro
import com.example.educanet.repository.ResenaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResenasRecientesUiState(
    val resenas: List<ResenaConLibro> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ResenasRecientesViewModel : ViewModel() {
    private val resenaRepository = ResenaRepository()

    private val _uiState = MutableStateFlow(ResenasRecientesUiState())
    val uiState: StateFlow<ResenasRecientesUiState> = _uiState.asStateFlow()

    init {
        cargarResenasRecientes()
    }

    fun cargarResenasRecientes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val resenas = resenaRepository.obtenerResenasRecientes(20)
                _uiState.value = _uiState.value.copy(
                    resenas = resenas,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar reseñas: ${e.message}"
                )
            }
        }
    }
}
