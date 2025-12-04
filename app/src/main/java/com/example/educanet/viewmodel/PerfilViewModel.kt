package com.example.educanet.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Usuario
import com.example.educanet.repository.PerfilRepository
import com.example.educanet.repository.PhotoRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Definimos el estado que esperan tus pantallas
data class PerfilUiState(
    val usuario: Usuario? = null,
    val fotoUrl: String? = null,
    val nombre: String = "",
    val isUploading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val isEditingName: Boolean = false
)

class PerfilViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var userDocListener: ListenerRegistration? = null
    private var ultimoUidCargado: String? = null  // Para detectar cambio de usuario

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // Crear repositorios internamente (sin necesidad de Factory)
    private val perfilRepository = PerfilRepository(firestore)
    private val photoRepository = PhotoRepository()

    // Limpiar estado (llamar al hacer logout)
    fun limpiarEstado() {
        userDocListener?.remove()
        userDocListener = null
        ultimoUidCargado = null
        _uiState.value = PerfilUiState()
        Log.d("EDUCA_DEBUG", "ViewModel: Estado limpiado")
    }

    fun cargarDatosIniciales() {
        val authUid = auth.currentUser?.uid ?: run {
            _uiState.update { it.copy(errorMessage = "No hay sesión activa") }
            return
        }
        
        // Si el usuario cambió, limpiar estado anterior
        if (ultimoUidCargado != null && ultimoUidCargado != authUid) {
            Log.d("EDUCA_DEBUG", "ViewModel: Usuario cambió de $ultimoUidCargado a $authUid, limpiando...")
            userDocListener?.remove()
            userDocListener = null
            _uiState.value = PerfilUiState()
        }
        
        ultimoUidCargado = authUid
        
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            try {
                Log.d("EDUCA_DEBUG", "ViewModel: Buscando perfil para uid: $authUid")
                val usuario = perfilRepository.obtenerPerfil(authUid)
                if (usuario != null) {
                    Log.d("EDUCA_DEBUG", "ViewModel: Usuario cargado: ${usuario.nombre}, Correo: ${usuario.correo}, DocId: ${usuario.id}")
                    _uiState.update {
                        it.copy(
                            usuario = usuario,
                            fotoUrl = usuario.fotoUrl,
                            nombre = usuario.nombre,
                            isUploading = false
                        )
                    }
                    // Observar cambios usando el ID del documento, no el authUid
                    observarFotoUrl(usuario.id)
                } else {
                    Log.e("EDUCA_DEBUG", "ViewModel: No se encontró usuario para uid: $authUid")
                    _uiState.update { it.copy(isUploading = false, errorMessage = "Usuario no encontrado") }
                }
            } catch (e: Exception) {
                Log.e("EDUCA_DEBUG", "Error cargando datos: ${e.message}", e)
                _uiState.update { it.copy(isUploading = false, errorMessage = e.message) }
            }
        }
    }

    // Subir a Storage usando PhotoRepository y guardar en Firestore
    private suspend fun subirFotoYGuardar(imageUri: Uri) {
        val uid = auth.currentUser?.uid ?: throw IllegalStateException("Usuario no autenticado")
        Log.d("EDUCA_DEBUG", "ViewModel: Iniciando subida de foto para uid: $uid")
        Log.d("EDUCA_DEBUG", "ViewModel: URI de imagen: $imageUri")
        
        // Nombre fijo para foto de perfil del usuario
        val nombreArchivo = "$uid/profile_${System.currentTimeMillis()}.jpg"
        
        // Subir la foto a Firebase Storage
        val url = photoRepository.subirFoto(imageUri, nombreArchivo)
        Log.d("EDUCA_DEBUG", "ViewModel: Foto subida, URL: $url")
        
        // Cache-busting para evitar imagen vieja
        val freshUrl = "$url?ts=${System.currentTimeMillis()}"

        // Determinar el ID de documento correcto en colección "usuario"
        val currentDocId = _uiState.value.usuario?.id ?: run {
            Log.d("EDUCA_DEBUG", "ViewModel: DocId no disponible, buscando...")
            // Si aún no tenemos el docID en UI, buscarlo por uid
            val usuario = perfilRepository.obtenerPerfil(uid)
            usuario?.id ?: throw IllegalStateException("No se encontró documento de usuario para uid=$uid")
        }
        
        Log.d("EDUCA_DEBUG", "ViewModel: Actualizando Firestore con docId: $currentDocId")

        // Guarda en colección "usuario" campo fotoUrl usando documentId
        perfilRepository.actualizarFotoPerfil(currentDocId, freshUrl)
        Log.d("EDUCA_DEBUG", "ViewModel: Firestore actualizado exitosamente")

        // Actualiza UI al instante
        val usuarioActualizado = _uiState.value.usuario?.copy(fotoUrl = freshUrl)
        _uiState.update { it.copy(usuario = usuarioActualizado, fotoUrl = freshUrl) }
    }

    // Método que llaman tus pantallas
    fun onImageSelectedAndSave(imageUri: Uri) {
        Log.d("EDUCA_DEBUG", "ViewModel: onImageSelectedAndSave llamado con: $imageUri")
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true, errorMessage = null) }
            try {
                subirFotoYGuardar(imageUri)
                Log.d("EDUCA_DEBUG", "ViewModel: Foto guardada exitosamente")
            } catch (e: Exception) {
                Log.e("PerfilViewModel", "Error al subir/guardar foto: ${e.message}", e)
                _uiState.update { it.copy(errorMessage = "Error al subir foto: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    private fun observarFotoUrl(documentId: String) {
        userDocListener?.remove()
        Log.d("EDUCA_DEBUG", "ViewModel: Observando documento: $documentId")
        userDocListener = firestore.collection("usuario")
            .document(documentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("EDUCA_DEBUG", "Error en listener: ${error.message}")
                    return@addSnapshotListener
                }
                val nuevaFoto = snapshot?.getString("fotoUrl") ?: ""
                val nuevoNombre = snapshot?.getString("nombre") ?: ""
                Log.d("EDUCA_DEBUG", "ViewModel: Nueva foto detectada: $nuevaFoto")
                if (nuevaFoto != _uiState.value.fotoUrl && nuevaFoto.isNotEmpty()) {
                    _uiState.update { it.copy(fotoUrl = nuevaFoto) }
                }
                if (nuevoNombre != _uiState.value.nombre && nuevoNombre.isNotEmpty()) {
                    _uiState.update { it.copy(nombre = nuevoNombre) }
                }
            }
    }

    // Función para actualizar el nombre del usuario
    fun actualizarNombre(nuevoNombre: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isEditingName = true, errorMessage = null) }
            try {
                val documentId = _uiState.value.usuario?.id
                if (documentId.isNullOrEmpty()) {
                    _uiState.update { it.copy(isEditingName = false, errorMessage = "No se encontró el usuario") }
                    return@launch
                }
                
                firestore.collection("usuario")
                    .document(documentId)
                    .update("nombre", nuevoNombre)
                    .await()
                
                // Actualizar UI
                val usuarioActualizado = _uiState.value.usuario?.copy(nombre = nuevoNombre)
                _uiState.update { 
                    it.copy(
                        usuario = usuarioActualizado,
                        nombre = nuevoNombre,
                        isEditingName = false,
                        successMessage = "Nombre actualizado correctamente"
                    ) 
                }
                Log.d("EDUCA_DEBUG", "ViewModel: Nombre actualizado a: $nuevoNombre")
            } catch (e: Exception) {
                Log.e("EDUCA_DEBUG", "Error al actualizar nombre: ${e.message}", e)
                _uiState.update { it.copy(isEditingName = false, errorMessage = "Error al actualizar: ${e.message}") }
            }
        }
    }

    fun clearSuccessMessage() {
        _uiState.update { it.copy(successMessage = null) }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        userDocListener?.remove()
        userDocListener = null
        super.onCleared()
    }
}