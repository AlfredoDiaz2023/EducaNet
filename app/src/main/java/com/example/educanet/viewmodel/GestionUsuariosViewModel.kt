package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.SolicitudVinculacion
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
    val fotoUrl: String = "",
    val alumnoVinculadoId: String = "",
    val alumnoVinculadoNombre: String = "",
    val alumnoVinculadoCorreo: String = ""
)

data class GestionUsuariosUiState(
    val usuarios: List<UsuarioAdmin> = emptyList(),
    val alumnos: List<UsuarioAdmin> = emptyList(),
    val alumnosDisponibles: List<UsuarioAdmin> = emptyList(),
    val solicitudesPendientes: List<SolicitudVinculacion> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val successMessage: String? = null,
    val showSolicitudes: Boolean = false
)

class GestionUsuariosViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow(GestionUsuariosUiState())
    val uiState: StateFlow<GestionUsuariosUiState> = _uiState.asStateFlow()

    init {
        escucharUsuarios()
        escucharSolicitudes()
    }

    private fun escucharUsuarios() {
        db.collection("usuario")
            .orderBy("nombre", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.value = _uiState.value.copy(error = error.message, isLoading = false)
                    return@addSnapshotListener
                }

                val usuarios = mutableListOf<UsuarioAdmin>()
                val alumnos = mutableListOf<UsuarioAdmin>()
                val alumnosVinculadosIds = mutableSetOf<String>()
                
                snapshot?.documents?.forEach { doc ->
                    val rol = doc.getString("rol") ?: ""
                    // Excluir administradores de la lista
                    if (rol == "Administrador") return@forEach
                    
                    val usuario = UsuarioAdmin(
                        id = doc.id,
                        uid = doc.getString("uid") ?: "",
                        nombre = doc.getString("nombre") ?: "",
                        correo = doc.getString("correo") ?: "",
                        clave = doc.getString("clave") ?: "",
                        rol = rol,
                        fotoUrl = doc.getString("fotoUrl") ?: "",
                        alumnoVinculadoId = doc.getString("alumnoVinculadoId") ?: "",
                        alumnoVinculadoNombre = doc.getString("alumnoVinculadoNombre") ?: "",
                        alumnoVinculadoCorreo = doc.getString("alumnoVinculadoCorreo") ?: ""
                    )
                    
                    usuarios.add(usuario)
                    if (rol == "Alumno") {
                        alumnos.add(usuario)
                    }
                    
                    // Registrar alumnos que ya están vinculados a apoderados
                    if (rol == "Apoderado" && usuario.alumnoVinculadoId.isNotEmpty()) {
                        alumnosVinculadosIds.add(usuario.alumnoVinculadoId)
                    }
                }

                // Filtrar alumnos disponibles (no vinculados)
                val alumnosDisponibles = alumnos.filter { alumno ->
                    alumno.id !in alumnosVinculadosIds
                }

                _uiState.value = _uiState.value.copy(
                    usuarios = usuarios,
                    alumnos = alumnos,
                    alumnosDisponibles = alumnosDisponibles,
                    isLoading = false
                )
            }
    }

    private fun escucharSolicitudes() {
        db.collection("solicitudes_vinculacion")
            .whereEqualTo("estado", "pendiente")
            .addSnapshotListener { snapshot, error ->
                if (error != null) return@addSnapshotListener
                
                val solicitudes = snapshot?.documents?.mapNotNull { doc ->
                    SolicitudVinculacion(
                        id = doc.id,
                        apoderadoId = doc.getString("apoderadoId") ?: "",
                        apoderadoNombre = doc.getString("apoderadoNombre") ?: "",
                        apoderadoCorreo = doc.getString("apoderadoCorreo") ?: "",
                        alumnoId = doc.getString("alumnoId") ?: "",
                        alumnoNombre = doc.getString("alumnoNombre") ?: "",
                        alumnoCorreo = doc.getString("alumnoCorreo") ?: "",
                        estado = doc.getString("estado") ?: "pendiente",
                        fechaSolicitud = doc.getLong("fechaSolicitud") ?: 0L
                    )
                } ?: emptyList()

                _uiState.value = _uiState.value.copy(solicitudesPendientes = solicitudes)
            }
    }

    fun toggleShowSolicitudes() {
        _uiState.value = _uiState.value.copy(showSolicitudes = !_uiState.value.showSolicitudes)
    }

    fun aprobarSolicitud(solicitud: SolicitudVinculacion) {
        viewModelScope.launch {
            try {
                // Verificar que el apoderadoId no esté vacío
                val apoderadoId = if (solicitud.apoderadoId.isNotEmpty()) {
                    // Verificar que el documento existe
                    val docExists = db.collection("usuario").document(solicitud.apoderadoId).get().await()
                    if (docExists.exists()) {
                        solicitud.apoderadoId
                    } else {
                        // El ID no existe, buscar por correo
                        null
                    }
                } else {
                    null
                }
                
                val finalApoderadoId = apoderadoId ?: run {
                    // Buscar el apoderado por correo si el ID está vacío o no existe
                    if (solicitud.apoderadoCorreo.isEmpty()) {
                        // Buscar por nombre como último recurso
                        val apoderadoSnapshot = db.collection("usuario")
                            .whereEqualTo("nombre", solicitud.apoderadoNombre)
                            .whereEqualTo("rol", "Apoderado")
                            .get()
                            .await()
                        
                        if (apoderadoSnapshot.isEmpty) {
                            _uiState.value = _uiState.value.copy(
                                error = "No se encontró el apoderado. Por favor, vincule manualmente desde la pestaña Apoderados."
                            )
                            return@launch
                        }
                        apoderadoSnapshot.documents.first().id
                    } else {
                        val apoderadoSnapshot = db.collection("usuario")
                            .whereEqualTo("correo", solicitud.apoderadoCorreo)
                            .get()
                            .await()
                        
                        if (apoderadoSnapshot.isEmpty) {
                            _uiState.value = _uiState.value.copy(
                                error = "No se encontró el apoderado con correo: ${solicitud.apoderadoCorreo}"
                            )
                            return@launch
                        }
                        apoderadoSnapshot.documents.first().id
                    }
                }

                // Actualizar el apoderado con la vinculación
                db.collection("usuario").document(finalApoderadoId).update(
                    mapOf(
                        "alumnoVinculadoId" to solicitud.alumnoId,
                        "alumnoVinculadoNombre" to solicitud.alumnoNombre,
                        "alumnoVinculadoCorreo" to solicitud.alumnoCorreo
                    )
                ).await()

                // Actualizar estado de la solicitud
                db.collection("solicitudes_vinculacion").document(solicitud.id).update(
                    mapOf(
                        "estado" to "aprobada",
                        "fechaRespuesta" to System.currentTimeMillis()
                    )
                ).await()

                _uiState.value = _uiState.value.copy(
                    successMessage = "Vinculación aprobada: ${solicitud.apoderadoNombre} → ${solicitud.alumnoNombre}"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al aprobar solicitud: ${e.message}"
                )
            }
        }
    }

    fun rechazarSolicitud(solicitud: SolicitudVinculacion) {
        viewModelScope.launch {
            try {
                db.collection("solicitudes_vinculacion").document(solicitud.id).update(
                    mapOf(
                        "estado" to "rechazada",
                        "fechaRespuesta" to System.currentTimeMillis()
                    )
                ).await()

                _uiState.value = _uiState.value.copy(
                    successMessage = "Solicitud rechazada"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al rechazar solicitud: ${e.message}"
                )
            }
        }
    }

    fun vincularAlumnoManual(apoderadoId: String, alumnoId: String, alumnoNombre: String, alumnoCorreo: String) {
        viewModelScope.launch {
            try {
                db.collection("usuario").document(apoderadoId).update(
                    mapOf(
                        "alumnoVinculadoId" to alumnoId,
                        "alumnoVinculadoNombre" to alumnoNombre,
                        "alumnoVinculadoCorreo" to alumnoCorreo
                    )
                ).await()

                _uiState.value = _uiState.value.copy(
                    successMessage = "Alumno vinculado correctamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al vincular alumno: ${e.message}"
                )
            }
        }
    }

    fun desvincularAlumno(apoderadoId: String) {
        viewModelScope.launch {
            try {
                db.collection("usuario").document(apoderadoId).update(
                    mapOf(
                        "alumnoVinculadoId" to "",
                        "alumnoVinculadoNombre" to "",
                        "alumnoVinculadoCorreo" to ""
                    )
                ).await()

                _uiState.value = _uiState.value.copy(
                    successMessage = "Alumno desvinculado correctamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = "Error al desvincular alumno: ${e.message}"
                )
            }
        }
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(error = null, successMessage = null)
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
