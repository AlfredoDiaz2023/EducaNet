package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ClaseVirtual
import com.example.educanet.model.Profesor
import com.example.educanet.repository.ClaseVirtualRepository
import com.example.educanet.repository.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AddClaseVirtualUiState(
    val nombre: String = "",
    val nivel: String = "",
    val meetUrl: String = "",
    val descripcion: String = "",
    val duracion: Int = 0,
    val isSaving: Boolean = false,
    val saveSuccess: Boolean = false,
    val errorMessage: String? = null
)

class AddClaseVirtualViewModel : ViewModel() {

    private val claseVirtualRepository = ClaseVirtualRepository()
    private val notificacionRepository = NotificacionRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(AddClaseVirtualUiState())
    val uiState: StateFlow<AddClaseVirtualUiState> = _uiState.asStateFlow()

    fun onNombreChange(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre)
    }

    fun onNivelChange(nivel: String) {
        _uiState.value = _uiState.value.copy(nivel = nivel)
    }

    fun onMeetUrlChange(meetUrl: String) {
        _uiState.value = _uiState.value.copy(meetUrl = meetUrl)
    }

    fun onDescripcionChange(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcion = descripcion)
    }
    fun onDuracionChange(duracion: String) {
        val duracionInt = duracion.toIntOrNull() ?: 0
        _uiState.value = _uiState.value.copy(duracion = duracionInt)
    }

    fun saveClaseVirtual() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)

            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Usuario no autenticado.", isSaving = false)
                    return@launch
                }

                // Buscar usuario por correo en la colección "usuario"
                val querySnapshot = db.collection("usuario")
                    .whereEqualTo("correo", currentUser.email)
                    .get()
                    .await()

                if (querySnapshot.isEmpty) {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se encontró el documento del usuario.", isSaving = false)
                    return@launch
                }

                val userDoc = querySnapshot.documents[0]
                val userRole = userDoc.getString("rol")

                if (userRole?.equals("Profesor", ignoreCase = true) != true) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Solo los profesores pueden agregar clases virtuales.", isSaving = false)
                    return@launch
                }
                
                val profesorNombre = userDoc.getString("nombre")
                if (profesorNombre == null) {
                    _uiState.value = _uiState.value.copy(errorMessage = "No se pudieron obtener los datos del profesor.", isSaving = false)
                    return@launch
                }

                val profesor = Profesor(nombre = profesorNombre, correo = currentUser.email ?: "")

                val claseVirtual = ClaseVirtual(
                    nombre = _uiState.value.nombre,
                    nivel = _uiState.value.nivel,
                    meet = _uiState.value.meetUrl,
                    descripcion = _uiState.value.descripcion,
                    duracion = _uiState.value.duracion,
                    profesor = profesor
                )

                val success = claseVirtualRepository.agregarClaseVirtual(claseVirtual)

                if (success) {
                    notificacionRepository.agregarNotificacion(
                        titulo = "Nueva clase virtual agregada",
                        mensaje = "Se ha agregado la clase: ${_uiState.value.nombre}"
                    )
                    _uiState.value = _uiState.value.copy(isSaving = false, saveSuccess = true)
                } else {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = "Error al guardar la clase virtual.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = e.message ?: "Ocurrió un error desconocido.")
            }
        }
    }
}
