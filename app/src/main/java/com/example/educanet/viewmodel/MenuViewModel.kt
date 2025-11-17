package com.example.educanet.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.*
import com.example.educanet.repository.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class MenuUiState(
    val usuario: Usuario? = null,
    val hasUnreadNotifications: Boolean = false,
    val isLoading: Boolean = true,
    val isUploadingPhoto: Boolean = false,
    val error: String? = null
)

class MenuViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val notificacionRepository = NotificacionRepository()

    private val _uiState = MutableStateFlow(MenuUiState())
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        checkForUnreadNotifications()
    }

    fun loadUserData() {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = "Usuario no autenticado.")
                return@launch
            }

            try {
                val userDoc = db.collection("users").document(user.uid).get().await()
                if (userDoc.exists()) {
                    val rol = userDoc.getString("rol")
                    val usuario: Usuario? = when (rol) {
                        "Alumno" -> userDoc.toObject(Alumno::class.java)
                        "Profesor" -> userDoc.toObject(Profesor::class.java)
                        "Apoderado" -> userDoc.toObject(Apoderado::class.java)
                        "Administrador" -> userDoc.toObject(Administrador::class.java)
                        else -> null
                    }
                    _uiState.value = _uiState.value.copy(usuario = usuario, isLoading = false)
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, error = "Documento de usuario no encontrado.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun checkForUnreadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(hasUnreadNotifications = notificacionRepository.hayNotificacionesSinLeer())
        }
    }

    fun updateProfilePicture(imageUri: String) {
        viewModelScope.launch {
            val user = auth.currentUser
            if (user == null) {
                _uiState.value = _uiState.value.copy(error = "No se puede cambiar la foto sin iniciar sesión.")
                return@launch
            }

            _uiState.value = _uiState.value.copy(isUploadingPhoto = true)
            try {
                val downloadUrl = uploadImageToStorage(Uri.parse(imageUri))
                db.collection("users").document(user.uid).update("fotoUrl", downloadUrl).await()

                // **LA SOLUCIÓN DEFINITIVA**
                // Forzar la actualización en la UI con una URL única para esta sesión.
                val cacheBustedUrl = "$downloadUrl&v=${System.currentTimeMillis()}"
                
                val currentUser = _uiState.value.usuario
                val updatedUser = when (currentUser) {
                    is Alumno -> currentUser.copy(fotoUrl = cacheBustedUrl)
                    is Profesor -> currentUser.copy(fotoUrl = cacheBustedUrl)
                    is Apoderado -> currentUser.copy(fotoUrl = cacheBustedUrl)
                    is Administrador -> currentUser.copy(fotoUrl = cacheBustedUrl)
                    null -> null
                }
                _uiState.value = _uiState.value.copy(usuario = updatedUser)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            } finally {
                _uiState.value = _uiState.value.copy(isUploadingPhoto = false)
            }
        }
    }

    private suspend fun uploadImageToStorage(uri: Uri): String {
        val storageRef = storage.reference
        val imageRef = storageRef.child("profile_pictures/${auth.currentUser!!.uid}")
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }
}
