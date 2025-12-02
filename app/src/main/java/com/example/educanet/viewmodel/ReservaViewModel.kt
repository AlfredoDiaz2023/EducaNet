package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Reserva
import com.example.educanet.repository.ReservaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReservaScreenUiState(
    val reservas: List<Reserva> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val confirmationMessage: String? = null
)

class ReservaViewModel : ViewModel() {

    private val reservaRepository = ReservaRepository()

    private val _uiState = MutableStateFlow(ReservaScreenUiState())
    val uiState: StateFlow<ReservaScreenUiState> = _uiState.asStateFlow()

    init {
        cargarReservas()
    }

    private fun cargarReservas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val reservas = reservaRepository.obtenerReservas()
                _uiState.value = _uiState.value.copy(reservas = reservas, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    fun eliminarReserva(reservaId: String) {
        viewModelScope.launch {
            val success = reservaRepository.eliminarReserva(reservaId)
            if (success) {
                // Actualizar la lista localmente para reflejar el cambio
                val updatedReservas = _uiState.value.reservas.filter { it.id != reservaId }
                _uiState.value = _uiState.value.copy(reservas = updatedReservas, confirmationMessage = "Reserva eliminada con éxito.")
            } else {
                _uiState.value = _uiState.value.copy(error = "Error al eliminar la reserva.")
            }
        }
    }

    fun messageShown() {
        _uiState.value = _uiState.value.copy(confirmationMessage = null)
    }

    fun refreshReservas() {
        cargarReservas()
    }
}
