package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class UsuarioAdmin(
    val id: String = "",
    val uid: String = "",
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val rol: String = "",
    val fotoUrl: String = ""
)

data class GestionUsuariosUiState(
    val usuarios: List<UsuarioAdmin> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null
)

class GestionUsuariosViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(GestionUsuariosUiState())
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    init {
        escucharUsuarios()
    }

    private fun escucharUsuarios() {
        db.collection("usuario")
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                    return@addSnapshotListener
                }

                val usuarios = snapshot?.documents?.mapNotNull { doc ->
                    val rol = doc.getString("rol") ?: ""
                    // Excluir administradores de la lista
                    if (rol == "Administrador") return@mapNotNull null
                    
                    UsuarioAdmin(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        nombre = doc.getString("nombre") ?: "",
                        correo = doc.getString("correo") ?: "",
                        clave = doc.getString("clave") ?: "",
                        rol = rol,
                        fotoUrl = doc.getString("fotoUrl") ?: ""
                    )
                } ?: emptyList()

                _uiState.value = _uiState.value.copy(usuarios = usuarios, isLoading = false)
            }
    }

    fun agregarUsuario(nombre: String, correo: String, clave: String, rol: String) {
        viewModelScope.launch {
            try {
                // Crear usuario en Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(correo, clave).await()
                val uid = authResult.user?.uid ?: ""

                val nuevoUsuario = hashMapOf(
                    "uid" to uid,
                    "nombre" to nombre,
                    "correo" to correo,
                    "clave" to clave,
                    "rol" to rol,
                    "fotoUrl" to "",
                    "fechaRegistro" to getCurrentDate()
                )
                db.collection("usuario").add(nuevoUsuario).await()
                _uiState.value = _uiState.value.copy(successMessage = "Usuario agregado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al agregar usuario: ${e.message}")
            }
        }
    }

    fun editarUsuario(id: String, nombre: String, correo: String, clave: String, rol: String) {
        viewModelScope.launch {
            try {
                val actualizacion = hashMapOf<String, Any>(
                    "nombre" to nombre,
                    "rol" to rol
                )
                
                // Solo actualizar clave si se proporcionó una nueva
                if (clave.isNotBlank()) {
                    actualizacion["clave"] = clave
                }
                
                db.collection("usuario").document(id).update(actualizacion).await()
                _uiState.value = _uiState.value.copy(successMessage = "Usuario actualizado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al editar usuario: ${e.message}")
            }
        }
    }

    fun eliminarUsuario(id: String, uid: String) {
        viewModelScope.launch {
            try {
                // Eliminar de Firestore
                db.collection("usuario").document(id).delete().await()
                
                // Nota: Eliminar usuario de Firebase Auth requiere que el usuario esté autenticado
                // o usar Firebase Admin SDK (backend). Por ahora solo eliminamos de Firestore.
                
                _uiState.value = _uiState.value.copy(successMessage = "Usuario eliminado correctamente")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = "Error al eliminar usuario: ${e.message}")
            }
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return sdf.format(Date())
    }
}
