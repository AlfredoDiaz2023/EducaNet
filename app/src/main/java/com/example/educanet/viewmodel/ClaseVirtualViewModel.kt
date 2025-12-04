package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.ClaseVirtual
import com.example.educanet.repository.ClaseVirtualRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ClaseVirtualViewModel : ViewModel() {
    private val repository = ClaseVirtualRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _clases = MutableStateFlow<List<ClaseVirtual>>(emptyList())
    val clases: StateFlow<List<ClaseVirtual>> = _clases.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _eliminando = MutableStateFlow(false)
    val eliminando: StateFlow<Boolean> = _eliminando.asStateFlow()

    private val _mensajeEliminacion = MutableStateFlow<String?>(null)
    val mensajeEliminacion: StateFlow<String?> = _mensajeEliminacion.asStateFlow()

    init {
        obtenerClases()
    }

    fun obtenerClases() {
        viewModelScope.launch {
            _cargando.value = true
            val resultado = repository.obtenerClasesVirtuales()
            _clases.value = resultado.clases
            _cargando.value = false
        }
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun eliminarClase(clase: ClaseVirtual, userRole: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _eliminando.value = true
            
            val currentEmail = auth.currentUser?.email
            val esAdmin = userRole.equals("Administrador", ignoreCase = true)
            val esProfesor = userRole.equals("Profesor", ignoreCase = true)
            val esPropietario = clase.profesor.correo == currentEmail
            
            // Verificar permisos
            val puedeEliminar = esAdmin || (esProfesor && esPropietario)
            
            if (!puedeEliminar) {
                _mensajeEliminacion.value = "No tienes permiso para eliminar esta clase"
                _eliminando.value = false
                onResult(false)
                return@launch
            }
            
            val resultado = repository.eliminarClaseVirtual(clase.id)
            
            if (resultado) {
                _mensajeEliminacion.value = "Clase eliminada correctamente"
                obtenerClases() // Recargar lista
            } else {
                _mensajeEliminacion.value = "Error al eliminar la clase"
            }
            
            _eliminando.value = false
            onResult(resultado)
        }
    }

    fun limpiarMensaje() {
        _mensajeEliminacion.value = null
    }
}
