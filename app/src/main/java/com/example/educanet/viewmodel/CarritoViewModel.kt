package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Libro
import com.example.educanet.model.Reserva
import com.example.educanet.repository.CarritoRepository
import com.example.educanet.repository.LibroRepository
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.repository.ReservaRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CarritoUiState(
    val items: List<Libro> = emptyList(),
    val libros: List<Libro> = emptyList(),
    val isConfirming: Boolean = false,
    val confirmationSuccess: Boolean = false,
    val confirmationMessage: String? = null,
    val error: String? = null
)

class CarritoViewModel : ViewModel() {

    private val reservaRepository = ReservaRepository()
    private val libroRepository = LibroRepository()
    private val carritoRepository = CarritoRepository()   // <-- asegurarse de tener este repo
    private val notificacionRepository = NotificacionRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState: StateFlow<CarritoUiState> = _uiState.asStateFlow()

    fun addToCart(libro: Libro) {
        val currentItems = _uiState.value.items.toMutableList()
        if (!currentItems.any { it.id == libro.id }) {
            currentItems.add(libro)
            _uiState.value = _uiState.value.copy(items = currentItems)
        }
    }

    fun removeFromCart(libro: Libro) {
        val currentItems = _uiState.value.items.toMutableList()
        currentItems.remove(libro)
        _uiState.value = _uiState.value.copy(items = currentItems)
    }

    fun confirmReservations(userName: String) {
        viewModelScope.launch {

            val carritoActual = _uiState.value.items

            if (carritoActual.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "El carrito está vacío.")
                return@launch
            }

            try {
                // 👉 1. Descontar stock como el botón SOLICITAR
                carritoActual.forEach { libro ->
                    libroRepository.actualizarStock(
                        libro.id,
                        (libro.cantidad - 1).coerceAtLeast(0)
                    )
                }

                // 👉 2. Crear todas las reservas (si tu lógica lo requiere)
                carritoActual.forEach { libro ->
                    val reserva = Reserva(
                        libroId = libro.id,
                        userId = auth.currentUser?.uid ?: "",
                        userName = userName,
                        libroNombre = libro.nombre
                    )
                    reservaRepository.agregarReserva(reserva)
                }

                // 👉 3. Vaciar carrito
                _uiState.value = _uiState.value.copy(items = emptyList())
                notificacionRepository.agregarNotificacion("Reservas confirmadas", "Tus reservas han sido confirmadas.")

                // 👉 4. Mensaje de éxito
                _uiState.value = _uiState.value.copy(
                    confirmationSuccess = true,
                    confirmationMessage = "Reservas confirmadas y stock actualizado"
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }


    fun messageShown() {
        _uiState.value = _uiState.value.copy(confirmationMessage = null, confirmationSuccess = false)
    }
}
