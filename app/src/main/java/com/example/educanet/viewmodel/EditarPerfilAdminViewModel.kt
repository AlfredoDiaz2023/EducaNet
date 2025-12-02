package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class EditarPerfilAdminUiState(
    val nombre: String = "",
    val correo: String = "",
    val documentoId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class EditarPerfilAdminViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(EditarPerfilAdminUiState())
    val uiState: StateFlow<EditarPerfilAdminUiState> = _uiState.asStateFlow()

    init {
        cargarDatosAdmin()
    }

    private fun cargarDatosAdmin() {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser != null) {
                    val correo = currentUser.email ?: ""
                    
                    val query = db.collection("usuario")
                        .whereEqualTo("correo", correo)
                        .get()
                        .await()

                    if (!query.isEmpty) {
                        val doc = query.documents[0]
                        _uiState.value = _uiState.value.copy(
                            nombre = doc.getString("nombre") ?: "",
                            correo = correo,
                            documentoId = doc.id
                        )
                    } else {
                        // Si es el admin hardcodeado
                        _uiState.value = _uiState.value.copy(
                            nombre = "Administrador",
                            correo = correo
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al cargar datos: ${e.message}")
            }
        }
    }

    fun actualizarNombre(nuevoNombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nuevoNombre)
    }

    fun guardarCambios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                if (_uiState.value.documentoId.isNotEmpty()) {
                    db.collection("usuario")
                        .document(_uiState.value.documentoId)
                        .update("nombre", _uiState.value.nombre)
                        .await()
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        successMessage = "Nombre actualizado correctamente"
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "No se encontró el documento del usuario"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al guardar: ${e.message}"
                )
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun limpiarError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
