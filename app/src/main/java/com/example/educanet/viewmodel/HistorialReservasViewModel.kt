package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

data class ReservaHistorial(
    val id: String = "",
    val libroId: String = "",
    val libroNombre: String = "",
    val userId: String = "",
    val userName: String = "",
    val timestamp: Date? = null
)

data class HistorialReservasUiState(
    val reservas: List<ReservaHistorial> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class HistorialReservasViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(HistorialReservasUiState())
    val uiState: StateFlow<HistorialReservasUiState> = _uiState.asStateFlow()

    init {
        escucharReservas()
    }

    private fun escucharReservas() {
        db.collection("reservas")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                    return@addSnapshotListener
                }

                val reservas = snapshot?.documents?.map { doc ->
                    ReservaHistorial(
                        id = doc.id,
                        libroId = doc.getString("libroId") ?: "",
                        libroNombre = doc.getString("libroNombre") ?: "",
                        userId = doc.getString("userId") ?: "",
                        userName = doc.getString("userName") ?: "",
                        timestamp = doc.getTimestamp("timestamp")?.toDate()
                    )
                } ?: emptyList()

                _uiState.value = _uiState.value.copy(reservas = reservas, isLoading = false)
            }
    }

    fun refrescar() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        escucharReservas()
    }
}
