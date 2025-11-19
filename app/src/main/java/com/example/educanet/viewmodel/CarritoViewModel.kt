package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Libro
import com.example.educanet.model.Reserva
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
    private val libroRepository = LibroRepository() // Añadir el repositorio de libros
    private val notificacionRepository = NotificacionRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState: StateFlow<CarritoUiState> = _uiState.asStateFlow()

    fun addToCart(libro: Libro) {
        val currentItems = _uiState.value.items.toMutableList()
        if (!currentItems.any { it.id == libro.id }) { // Evitar duplicados
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
            val user = auth.currentUser
            if (user == null) {
                _uiState.value = _uiState.value.copy(error = "Debes iniciar sesión para reservar.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isConfirming = true)

            try {
                val itemsToReserve = _uiState.value.items
                if (itemsToReserve.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        error = "El carrito está vacío.",
                        isConfirming = false
                    )
                    return@launch
                }

                var allSuccess = true

                for (libro in itemsToReserve) {

                    // 1️⃣ DESCONTAR STOCK AUNQUE SEA 0 (sin bloquear reserva)
                    val nuevoStock = if (libro.cantidad > 0) libro.cantidad - 1 else 0

                    val stockUpdated = libroRepository.actualizarStock(libro.id, nuevoStock)
                    if (!stockUpdated) {
                        allSuccess = false
                    }

                    // 2️⃣ SIEMPRE crear la reserva
                    val reserva = Reserva(
                        libroId = libro.id,
                        userId = user.uid,
                        userName = userName,
                        libroNombre = libro.nombre
                    )

                    val reservaSuccess = reservaRepository.agregarReserva(reserva)
                    if (!reservaSuccess) {
                        allSuccess = false
                    }
                }

                // 3️⃣ Notificación + éxito
                if (allSuccess) {
                    val bookNames = itemsToReserve.joinToString(", ") { it.nombre }

                    notificacionRepository.agregarNotificacion(
                        titulo = "Nuevas reservas creadas",
                        mensaje = "El usuario $userName ha reservado los siguientes libros: $bookNames"
                    )

                    // Limpiar carrito y mostrar éxito
                    _uiState.value = CarritoUiState(
                        confirmationSuccess = true,
                        confirmationMessage = "¡Reservas confirmadas con éxito!"
                    )

                } else {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al crear o actualizar una o más reservas.",
                        isConfirming = false
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message,
                    isConfirming = false
                )
            }
        }
    }


    fun messageShown() {
        _uiState.value = _uiState.value.copy(confirmationMessage = null, confirmationSuccess = false) // Resetear ambos estados
    }
}
