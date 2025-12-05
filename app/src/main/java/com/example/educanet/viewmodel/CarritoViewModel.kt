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
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CarritoItem(
    val libro: Libro,
    val cantidad: Int = 1
)

data class CarritoUiState(
    val items: List<CarritoItem> = emptyList(),
    val libros: List<Libro> = emptyList(),
    val isConfirming: Boolean = false,
    val confirmationSuccess: Boolean = false,
    val confirmationMessage: String? = null,
    val error: String? = null
)

class CarritoViewModel : ViewModel() {

    private val reservaRepository = ReservaRepository()
    private val libroRepository = LibroRepository()
    private val carritoRepository = CarritoRepository()
    private val notificacionRepository = NotificacionRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(CarritoUiState())
    val uiState: StateFlow<CarritoUiState> = _uiState.asStateFlow()

    fun addToCart(libro: Libro) {
        val currentItems = _uiState.value.items.toMutableList()
        val existingIndex = currentItems.indexOfFirst { it.libro.id == libro.id }
        
        if (existingIndex >= 0) {
            // Si ya existe, incrementar la cantidad
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(cantidad = existingItem.cantidad + 1)
        } else {
            // Si no existe, agregar nuevo item con cantidad 1
            currentItems.add(CarritoItem(libro = libro, cantidad = 1))
        }
        _uiState.value = _uiState.value.copy(items = currentItems)
    }

    fun removeFromCart(libro: Libro) {
        viewModelScope.launch {
            val currentItems = _uiState.value.items.toMutableList()
            val itemToRemove = currentItems.find { it.libro.id == libro.id }
            
            if (itemToRemove != null) {
                // Devolver el stock al libro (cantidad de items en carrito)
                try {
                    val libroActual = libroRepository.obtenerLibroPorId(libro.id)
                    if (libroActual != null) {
                        val nuevoStock = libroActual.cantidad + itemToRemove.cantidad
                        libroRepository.actualizarStock(libro.id, nuevoStock)
                    }
                } catch (e: Exception) {
                    Log.e("CarritoViewModel", "Error devolviendo stock: ${e.message}")
                }
                
                // Remover del carrito
                currentItems.removeAll { it.libro.id == libro.id }
                _uiState.value = _uiState.value.copy(items = currentItems)
            }
        }
    }

    fun confirmReservations(userName: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isConfirming = true)

            val carritoActual = _uiState.value.items
            val userId = auth.currentUser?.uid ?: ""

            if (carritoActual.isEmpty()) {
                _uiState.value = _uiState.value.copy(error = "El carrito está vacío.", isConfirming = false)
                return@launch
            }

            try {
                // El stock ya se descontó al presionar "Solicitar"
                // Solo crear las reservas en la base de datos
                carritoActual.forEach { carritoItem ->
                    // Crear una reserva por cada cantidad
                    repeat(carritoItem.cantidad) {
                        val reserva = Reserva(
                            libroId = carritoItem.libro.id,
                            userId = userId,
                            userName = userName,
                            libroNombre = carritoItem.libro.nombre
                        )
                        reservaRepository.agregarReserva(reserva)
                    }
                }

                // Vaciar carrito
                _uiState.value = _uiState.value.copy(items = emptyList())

                // Mensaje de éxito
                _uiState.value = _uiState.value.copy(
                    isConfirming = false,
                    confirmationSuccess = true,
                    confirmationMessage = "¡Libros confirmados exitosamente!"
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isConfirming = false)
            }
        }
    }


    fun messageShown() {
        _uiState.value = _uiState.value.copy(confirmationMessage = null, confirmationSuccess = false)
    }
}
