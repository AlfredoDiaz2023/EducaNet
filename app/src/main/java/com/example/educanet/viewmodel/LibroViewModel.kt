package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Libro
import com.example.educanet.model.Resena
import com.example.educanet.repository.LibroRepository
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.repository.ResenaRepository
import com.example.educanet.repository.ReservaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibroScreenUiState(
    val libros: List<Libro> = emptyList(),
    val resenas: List<Resena> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
)

class LibroViewModel : ViewModel() {

    private val libroRepository = LibroRepository()
    private val resenaRepository = ResenaRepository()
    private val reservaRepository = ReservaRepository()
    private val notificacionRepository = NotificacionRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(LibroScreenUiState())
    val uiState: StateFlow<LibroScreenUiState> = _uiState.asStateFlow()

    init {
        escucharLibrosTiempoReal()   // 👈🔥 ahora sí en tiempo real
    }

    // CAMBIO IMPORTANTE: Listener en tiempo real
    private fun escucharLibrosTiempoReal() {
        _uiState.value = _uiState.value.copy(isLoading = true)

        db.collection("libro")
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val librosActualizados = snapshot.documents.map { document ->
                        Libro(
                            id = document.id,
                            nombre = document.getString("nombre") ?: "",
                            nivel = document.getString("nivel") ?: "",
                            imagen = document.getString("imagen") ?: "",
                            cantidad = document.getLong("cantidad")?.toInt() ?: 0
                        )
                    }

                    // Actualiza UI automáticamente
                    _uiState.value = _uiState.value.copy(libros = librosActualizados, isLoading = false)
                }
            }
    }

    fun obtenerResenas(libroId: String) {
        viewModelScope.launch {
            try {
                val resenas = resenaRepository.obtenerResenas(libroId)
                _uiState.value = _uiState.value.copy(resenas = resenas)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun agregarResena(libroId: String, rating: Float, comment: String) {
        viewModelScope.launch {
            val user = auth.currentUser ?: run {
                _uiState.value = _uiState.value.copy(error = "Debes iniciar sesión para dejar una reseña.")
                return@launch
            }

            // Buscar el nombre del usuario en Firestore
            var userName = "Usuario"
            try {
                val querySnapshot = db.collection("usuario")
                    .whereEqualTo("correo", user.email)
                    .get()
                    .await()
                
                if (!querySnapshot.isEmpty) {
                    userName = querySnapshot.documents[0].getString("nombre") ?: "Usuario"
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            val resena = Resena(
                itemId = libroId,
                userId = user.uid,
                userName = userName,
                rating = rating,
                comment = comment
            )

            val success = resenaRepository.agregarResena(resena)
            if (success) {
                obtenerResenas(libroId)
                _uiState.value = _uiState.value.copy(successMessage = "¡Reseña agregada exitosamente!")
            } else {
                _uiState.value = _uiState.value.copy(error = "Error al agregar la reseña.")
            }
        }
    }

    fun solicitarLibro(libro: Libro, nombreUsuario: String, rolUsuario: String, userId: String) {
        viewModelScope.launch {
            if (libro.cantidad > 0) {
                val nuevoStock = libro.cantidad - 1
                val success = libroRepository.actualizarStock(libro.id, nuevoStock)

                if (success) {
                    // Notificación para el usuario que solicitó
                    notificacionRepository.agregarNotificacionParaUsuario(
                        titulo = "📚 Libro solicitado exitosamente",
                        mensaje = "Has solicitado el libro: ${libro.nombre}. ¡Recógelo en biblioteca!",
                        userId = userId
                    )
                    // Notificación para el administrador
                    notificacionRepository.agregarNotificacionParaAdmin(
                        titulo = "📚 Nueva solicitud de libro",
                        mensaje = "$nombreUsuario ($rolUsuario) ha solicitado: ${libro.nombre}"
                    )
                    _uiState.value = _uiState.value.copy(successMessage = "¡Libro solicitado exitosamente!")
                } else {
                    _uiState.value = _uiState.value.copy(error = "Error al solicitar el libro.")
                }
            } else {
                _uiState.value = _uiState.value.copy(error = "Libro sin stock")
            }
        }
    }

    fun reservarLibro(libro: Libro, nombreUsuario: String, rolUsuario: String) {
        viewModelScope.launch {
            if (libro.cantidad > 0) {
                val nuevoStock = libro.cantidad - 1
                val success = libroRepository.actualizarStock(libro.id, nuevoStock)

                if (success) {
                    notificacionRepository.agregarNotificacion(
                        titulo = "Reserva de libro",
                        mensaje = "El usuario $nombreUsuario ($rolUsuario) ha reservado el libro: ${libro.nombre}"
                    )
                    _uiState.value = _uiState.value.copy(successMessage = "¡Libro reservado exitosamente!")
                } else {
                    _uiState.value = _uiState.value.copy(error = "Error al reservar el libro.")
                }
            } else {
                _uiState.value = _uiState.value.copy(error = "Libro sin stock")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(successMessage = null, error = null)
    }

    fun reservarLibroAlCarrito(libro: Libro) {
        _uiState.value = _uiState.value.copy(successMessage = "¡Libro agregado al carrito!")
    }

    fun mostrarErrorSinStock() {
        _uiState.value = _uiState.value.copy(error = "Libro sin stock")
    }
}
