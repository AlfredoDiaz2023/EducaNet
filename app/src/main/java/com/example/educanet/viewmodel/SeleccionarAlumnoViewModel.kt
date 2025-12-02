package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Alumno
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SeleccionarAlumnoViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()

    private val _alumnos = MutableStateFlow<List<Alumno>>(emptyList())
    val alumnos: StateFlow<List<Alumno>> = _alumnos

    private val _cargando = MutableStateFlow(false)
    val cargando: StateFlow<Boolean> = _cargando

    private val _alumnoSeleccionado = MutableStateFlow<Alumno?>(null)
    val alumnoSeleccionado: StateFlow<Alumno?> = _alumnoSeleccionado

    private val _guardadoExitoso = MutableStateFlow(false)
    val guardadoExitoso: StateFlow<Boolean> = _guardadoExitoso

    private val _errorMensaje = MutableStateFlow("")
    val errorMensaje: StateFlow<String> = _errorMensaje

    fun cargarAlumnos() {
        viewModelScope.launch {
            _cargando.value = true
            try {
                val snapshot = db.collection("usuario")
                    .whereEqualTo("rol", "Alumno")
                    .get()
                    .await()

                val listaAlumnos = snapshot.documents.mapNotNull { doc ->
                    try {
                        Alumno(
                            id = doc.id,
                            nombre = doc.getString("nombre") ?: "",
                            correo = doc.getString("correo") ?: "",
                            clave = doc.getString("clave") ?: "",
                            rol = doc.getString("rol") ?: "Alumno",
                            fotoUrl = doc.getString("fotoUrl") ?: "",
                            fechaRegistro = doc.getString("fechaRegistro") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                _alumnos.value = listaAlumnos
            } catch (e: Exception) {
                _errorMensaje.value = "Error al cargar alumnos: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun seleccionarAlumno(alumno: Alumno) {
        _alumnoSeleccionado.value = alumno
    }

    fun guardarVinculacion(correoApoderado: String) {
        val alumno = _alumnoSeleccionado.value ?: return
        
        viewModelScope.launch {
            _cargando.value = true
            try {
                // Buscar el documento del apoderado por correo
                val apoderadoSnapshot = db.collection("usuario")
                    .whereEqualTo("correo", correoApoderado)
                    .get()
                    .await()

                if (apoderadoSnapshot.isEmpty) {
                    _errorMensaje.value = "No se encontró el apoderado"
                    return@launch
                }

                val apoderadoDoc = apoderadoSnapshot.documents.first()
                
                // Actualizar el documento del apoderado con el alumno vinculado
                db.collection("usuario")
                    .document(apoderadoDoc.id)
                    .update(mapOf(
                        "alumnoVinculadoId" to alumno.id,
                        "alumnoVinculadoNombre" to alumno.nombre,
                        "alumnoVinculadoCorreo" to alumno.correo
                    ))
                    .await()

                _guardadoExitoso.value = true
            } catch (e: Exception) {
                _errorMensaje.value = "Error al vincular alumno: ${e.message}"
            } finally {
                _cargando.value = false
            }
        }
    }

    fun resetEstado() {
        _guardadoExitoso.value = false
        _errorMensaje.value = ""
        _alumnoSeleccionado.value = null
    }
}
