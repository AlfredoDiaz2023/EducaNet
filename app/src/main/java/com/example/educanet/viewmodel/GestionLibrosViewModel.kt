package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Libro
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class GestionLibrosUiState(
    val libros: List<Libro> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
)

class GestionLibrosViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(GestionLibrosUiState())
    val uiState: StateFlow<GestionLibrosUiState> = _uiState.asStateFlow()

    init {
        escucharLibros()
    }

    private fun escucharLibros() {
        db.collection("libro")
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                    return@addSnapshotListener
                }

                val libros = snapshot?.documents?.map { doc ->
                    Libro(
                        id = doc.id,
                        nombre = doc.getString("nombre") ?: "",
                        nivel = doc.getString("nivel") ?: "",
                        imagen = doc.getString("imagen") ?: "",
                        cantidad = doc.getLong("cantidad")?.toInt() ?: 0
                    )
                } ?: emptyList()

                _uiState.value = _uiState.value.copy(libros = libros, isLoading = false)
            }
    }

    fun agregarLibro(nombre: String, nivel: String, imagen: String, cantidad: Int) {
        viewModelScope.launch {
            try {
                val nuevoLibro = hashMapOf(
                    "nombre" to nombre,
                    "nivel" to nivel,
                    "imagen" to imagen,
                    "cantidad" to cantidad
                )
                db.collection("libro").add(nuevoLibro).await()
                _uiState.value = _uiState.value.copy(successMessage = "Libro agregado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al agregar libro: ${e.message}")
            }
        }
    }

    fun editarLibro(id: String, nombre: String, nivel: String, imagen: String, cantidad: Int) {
        viewModelScope.launch {
            try {
                val actualizacion = hashMapOf<String, Any>(
                    "nombre" to nombre,
                    "nivel" to nivel,
                    "imagen" to imagen,
                    "cantidad" to cantidad
                )
                db.collection("libro").document(id).update(actualizacion).await()
                _uiState.value = _uiState.value.copy(successMessage = "Libro actualizado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al editar libro: ${e.message}")
            }
        }
    }

    fun eliminarLibro(id: String) {
        viewModelScope.launch {
            try {
                db.collection("libro").document(id).delete().await()
                _uiState.value = _uiState.value.copy(successMessage = "Libro eliminado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al eliminar libro: ${e.message}")
            }
        }
    }
}
