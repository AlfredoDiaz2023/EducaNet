package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Alumno
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.model.SolicitudVinculacion
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ApoderadoUiState(
    val nombreApoderado: String = "",
    val correoApoderado: String = "",
    val apoderadoDocId: String = "",
    val fotoUrlApoderado: String = "",
    val alumnoVinculado: AlumnoVinculadoInfo? = null,
    val notasAlumno: List<ProgresoAcademico> = emptyList(),
    val alumnosDisponibles: List<AlumnoVinculadoInfo> = emptyList(),
    val solicitudPendiente: SolicitudVinculacion? = null,
    val cargando: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val showSolicitudDialog: Boolean = false
)

data class AlumnoVinculadoInfo(
    val id: String = "",
    val nombre: String = "",
    val correo: String = "",
    val fotoUrl: String = ""
)

class ApoderadoViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(ApoderadoUiState())
    val uiState: StateFlow<ApoderadoUiState> = _uiState

    fun cargarDatosApoderado() {
        val currentUser = auth.currentUser ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            try {
                // Buscar datos del apoderado por correo
                val apoderadoSnapshot = db.collection("usuario")
                    .whereEqualTo("correo", currentUser.email)
                    .get()
                    .await()

                if (apoderadoSnapshot.isEmpty) {
                    _uiState.value = _uiState.value.copy(
                        cargando = false,
                        errorMessage = "No se encontró el apoderado"
                    )
                    return@launch
                }

                val apoderadoDoc = apoderadoSnapshot.documents.first()
                val nombreApoderado = apoderadoDoc.getString("nombre") ?: ""
                val correoApoderado = apoderadoDoc.getString("correo") ?: ""
                val fotoUrlApoderado = apoderadoDoc.getString("fotoUrl") ?: ""
                val alumnoVinculadoId = apoderadoDoc.getString("alumnoVinculadoId") ?: ""
                val alumnoVinculadoNombre = apoderadoDoc.getString("alumnoVinculadoNombre") ?: ""
                val alumnoVinculadoCorreo = apoderadoDoc.getString("alumnoVinculadoCorreo") ?: ""

                Log.d("ApoderadoVM", "=== Datos del Apoderado ===")
                Log.d("ApoderadoVM", "Nombre: $nombreApoderado")
                Log.d("ApoderadoVM", "Correo: $correoApoderado")
                Log.d("ApoderadoVM", "DocID: ${apoderadoDoc.id}")
                Log.d("ApoderadoVM", "=== Datos del Alumno Vinculado ===")
                Log.d("ApoderadoVM", "Alumno vinculado ID: '$alumnoVinculadoId'")
                Log.d("ApoderadoVM", "Alumno vinculado Nombre: '$alumnoVinculadoNombre'")
                Log.d("ApoderadoVM", "Alumno vinculado Correo: '$alumnoVinculadoCorreo'")
                Log.d("ApoderadoVM", "Todos los campos del doc: ${apoderadoDoc.data}")

                _uiState.value = _uiState.value.copy(
                    nombreApoderado = nombreApoderado,
                    correoApoderado = correoApoderado,
                    apoderadoDocId = apoderadoDoc.id,
                    fotoUrlApoderado = fotoUrlApoderado
                )

                // Si hay alumno vinculado (verificar ID o correo)
                if (alumnoVinculadoId.isNotEmpty() || alumnoVinculadoCorreo.isNotEmpty()) {
                    Log.d("ApoderadoVM", "Alumno vinculado detectado, creando AlumnoVinculadoInfo")
                    
                    // Si el nombre está vacío pero tenemos correo, intentar buscar el nombre del alumno
                    var nombreFinal = alumnoVinculadoNombre
                    if (nombreFinal.isEmpty() && alumnoVinculadoCorreo.isNotEmpty()) {
                        Log.d("ApoderadoVM", "Nombre vacío, buscando por correo...")
                        try {
                            val alumnoSnapshot = db.collection("usuario")
                                .whereEqualTo("correo", alumnoVinculadoCorreo)
                                .get()
                                .await()
                            if (!alumnoSnapshot.isEmpty) {
                                nombreFinal = alumnoSnapshot.documents.first().getString("nombre") ?: ""
                                Log.d("ApoderadoVM", "Nombre encontrado: $nombreFinal")
                            }
                        } catch (e: Exception) {
                            Log.e("ApoderadoVM", "Error buscando nombre del alumno: ${e.message}")
                        }
                    }
                    
                    val alumnoInfo = AlumnoVinculadoInfo(
                        id = alumnoVinculadoId,
                        nombre = nombreFinal,
                        correo = alumnoVinculadoCorreo
                    )
                    Log.d("ApoderadoVM", "AlumnoVinculadoInfo creado: $alumnoInfo")
                    _uiState.value = _uiState.value.copy(
                        alumnoVinculado = alumnoInfo,
                        cargando = false
                    )

                    // Cargar notas del alumno
                    if (alumnoVinculadoCorreo.isNotEmpty()) {
                        cargarNotasAlumno(alumnoVinculadoCorreo)
                    }
                } else {
                    Log.d("ApoderadoVM", "No hay alumno vinculado, verificando solicitudes pendientes")
                    // No tiene alumno vinculado, verificar si hay solicitud pendiente
                    verificarSolicitudPendiente(apoderadoDoc.id)
                    _uiState.value = _uiState.value.copy(cargando = false)
                }

            } catch (e: Exception) {
                Log.e("ApoderadoVM", "Error: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    cargando = false,
                    errorMessage = "Error al cargar datos: ${e.message}"
                )
            }
        }
    }

    private suspend fun verificarSolicitudPendiente(apoderadoId: String) {
        try {
            val solicitudSnapshot = db.collection("solicitudes_vinculacion")
                .whereEqualTo("apoderadoId", apoderadoId)
                .whereEqualTo("estado", "pendiente")
                .get()
                .await()

            if (!solicitudSnapshot.isEmpty) {
                val doc = solicitudSnapshot.documents.first()
                val solicitud = SolicitudVinculacion(
                    id = doc.id,
                    apoderadoId = doc.getString("apoderadoId") ?: "",
                    apoderadoNombre = doc.getString("apoderadoNombre") ?: "",
                    apoderadoCorreo = doc.getString("apoderadoCorreo") ?: "",
                    alumnoId = doc.getString("alumnoId") ?: "",
                    alumnoNombre = doc.getString("alumnoNombre") ?: "",
                    alumnoCorreo = doc.getString("alumnoCorreo") ?: "",
                    fechaSolicitud = doc.getLong("fechaSolicitud") ?: 0L,
                    estado = doc.getString("estado") ?: "pendiente"
                )
                _uiState.value = _uiState.value.copy(solicitudPendiente = solicitud)
            }
        } catch (e: Exception) {
            Log.e("ApoderadoVM", "Error verificando solicitud: ${e.message}")
        }
    }

    fun cargarAlumnosDisponibles() {
        viewModelScope.launch {
            try {
                // Primero obtener todos los alumnos vinculados a apoderados
                val apoderadosSnapshot = db.collection("usuario")
                    .whereEqualTo("rol", "Apoderado")
                    .get()
                    .await()
                
                val alumnosVinculadosIds = apoderadosSnapshot.documents.mapNotNull { doc ->
                    doc.getString("alumnoVinculadoId")
                }.filter { it.isNotEmpty() }.toSet()

                // También obtener alumnos con solicitudes pendientes
                val solicitudesPendientes = db.collection("solicitudes_vinculacion")
                    .whereEqualTo("estado", "pendiente")
                    .get()
                    .await()
                
                val alumnosConSolicitudPendiente = solicitudesPendientes.documents.mapNotNull { doc ->
                    doc.getString("alumnoId")
                }.filter { it.isNotEmpty() }.toSet()

                // Obtener todos los alumnos
                val alumnosSnapshot = db.collection("usuario")
                    .whereEqualTo("rol", "Alumno")
                    .get()
                    .await()

                // Filtrar solo los que NO están vinculados y NO tienen solicitud pendiente
                val alumnosDisponibles = alumnosSnapshot.documents.mapNotNull { doc ->
                    val alumnoId = doc.id
                    // Excluir si ya está vinculado o tiene solicitud pendiente
                    if (alumnoId in alumnosVinculadosIds || alumnoId in alumnosConSolicitudPendiente) {
                        null
                    } else {
                        AlumnoVinculadoInfo(
                            id = alumnoId,
                            nombre = doc.getString("nombre") ?: "",
                            correo = doc.getString("correo") ?: "",
                            fotoUrl = doc.getString("fotoUrl") ?: ""
                        )
                    }
                }

                _uiState.value = _uiState.value.copy(
                    alumnosDisponibles = alumnosDisponibles,
                    showSolicitudDialog = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cargar alumnos: ${e.message}"
                )
            }
        }
    }

    fun enviarSolicitudVinculacion(alumno: AlumnoVinculadoInfo) {
        viewModelScope.launch {
            try {
                val currentUser = auth.currentUser
                if (currentUser == null) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Usuario no autenticado"
                    )
                    return@launch
                }

                // Siempre buscar los datos del apoderado frescos de Firestore
                val apoderadoSnapshot = db.collection("usuario")
                    .whereEqualTo("correo", currentUser.email)
                    .get()
                    .await()
                
                if (apoderadoSnapshot.isEmpty) {
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No se encontró el apoderado"
                    )
                    return@launch
                }

                val apoderadoDoc = apoderadoSnapshot.documents.first()
                val apoderadoId = apoderadoDoc.id
                val apoderadoNombre = apoderadoDoc.getString("nombre") ?: ""
                val apoderadoCorreo = apoderadoDoc.getString("correo") ?: currentUser.email ?: ""
                
                Log.d("ApoderadoVM", "Enviando solicitud - ID: $apoderadoId, Nombre: $apoderadoNombre, Correo: $apoderadoCorreo")
                
                val solicitud = hashMapOf(
                    "apoderadoId" to apoderadoId,
                    "apoderadoNombre" to apoderadoNombre,
                    "apoderadoCorreo" to apoderadoCorreo,
                    "alumnoId" to alumno.id,
                    "alumnoNombre" to alumno.nombre,
                    "alumnoCorreo" to alumno.correo,
                    "fechaSolicitud" to System.currentTimeMillis(),
                    "estado" to "pendiente"
                )

                val docRef = db.collection("solicitudes_vinculacion").add(solicitud).await()
                
                val nuevaSolicitud = SolicitudVinculacion(
                    id = docRef.id,
                    apoderadoId = apoderadoId,
                    apoderadoNombre = apoderadoNombre,
                    apoderadoCorreo = apoderadoCorreo,
                    alumnoId = alumno.id,
                    alumnoNombre = alumno.nombre,
                    alumnoCorreo = alumno.correo,
                    estado = "pendiente"
                )

                _uiState.value = _uiState.value.copy(
                    apoderadoDocId = apoderadoId,
                    nombreApoderado = apoderadoNombre,
                    correoApoderado = apoderadoCorreo,
                    solicitudPendiente = nuevaSolicitud,
                    showSolicitudDialog = false,
                    successMessage = "Solicitud enviada correctamente. El administrador la revisará pronto."
                )
            } catch (e: Exception) {
                Log.e("ApoderadoVM", "Error enviando solicitud: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al enviar solicitud: ${e.message}"
                )
            }
        }
    }

    fun cancelarSolicitud() {
        viewModelScope.launch {
            try {
                val solicitud = _uiState.value.solicitudPendiente ?: return@launch
                
                db.collection("solicitudes_vinculacion").document(solicitud.id).delete().await()
                
                _uiState.value = _uiState.value.copy(
                    solicitudPendiente = null,
                    successMessage = "Solicitud cancelada"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error al cancelar solicitud: ${e.message}"
                )
            }
        }
    }

    fun cerrarDialogoSolicitud() {
        _uiState.value = _uiState.value.copy(showSolicitudDialog = false)
    }

    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(
            errorMessage = null,
            successMessage = null
        )
    }

    private suspend fun cargarNotasAlumno(correoAlumno: String) {
        try {
            Log.d("ApoderadoVM", "Cargando notas para: $correoAlumno")
            
            val notasSnapshot = db.collection("progreso_academico")
                .whereEqualTo("alumno", correoAlumno)
                .get()
                .await()

            val notas = notasSnapshot.documents.mapNotNull { doc ->
                try {
                    ProgresoAcademico(
                        id = doc.id,
                        alumno = doc.getString("alumno") ?: "",
                        asignatura = doc.getString("asignatura") ?: "",
                        notas = doc.getDouble("notas") ?: 0.0,
                        profesor = doc.getString("profesor") ?: "",
                        curso = doc.getString("curso") ?: ""
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Log.d("ApoderadoVM", "Notas cargadas: ${notas.size}")
            
            _uiState.value = _uiState.value.copy(
                notasAlumno = notas,
                cargando = false
            )
        } catch (e: Exception) {
            Log.e("ApoderadoVM", "Error cargando notas: ${e.message}")
            _uiState.value = _uiState.value.copy(
                cargando = false,
                errorMessage = "Error al cargar notas: ${e.message}"
            )
        }
    }

    fun refreshData() {
        cargarDatosApoderado()
    }
}
