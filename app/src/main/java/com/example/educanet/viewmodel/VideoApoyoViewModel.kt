package com.example.educanet.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.VideoApoyo
import com.example.educanet.repository.VideoApoyoRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VideoApoyoViewModel : ViewModel() {
    private val repository = VideoApoyoRepository()
    private val auth = FirebaseAuth.getInstance()

    private val _videos = MutableStateFlow<List<VideoApoyo>>(emptyList())
    val videos: StateFlow<List<VideoApoyo>> = _videos.asStateFlow()

    private val _cargando = MutableStateFlow(true)
    val cargando: StateFlow<Boolean> = _cargando.asStateFlow()

    private val _eliminando = MutableStateFlow(false)
    val eliminando: StateFlow<Boolean> = _eliminando.asStateFlow()

    private val _mensajeEliminacion = MutableStateFlow<String?>(null)
    val mensajeEliminacion: StateFlow<String?> = _mensajeEliminacion.asStateFlow()
    
    private var cursoActual: String = ""

    init {
        obtenerVideos()
    }

    private fun obtenerVideos() {
        viewModelScope.launch {
            _cargando.value = true
            val resultado = if (cursoActual.isNotBlank()) {
                repository.obtenerVideosPorCurso(cursoActual)
            } else {
                repository.obtenerVideosDeApoyo()
            }
            _videos.value = resultado.videos
            _cargando.value = false
        }
    }

    fun cargarVideos() {
        obtenerVideos()
    }
    
    /**
     * Carga videos filtrados por curso
     * Si el curso está vacío (para profesores/admin), carga todos
     */
    fun cargarVideosPorCurso(curso: String) {
        cursoActual = curso
        obtenerVideos()
    }

    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun eliminarVideo(video: VideoApoyo, userRole: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _eliminando.value = true
            
            val currentEmail = auth.currentUser?.email
            val esAdmin = userRole.equals("Administrador", ignoreCase = true)
            val esProfesor = userRole.equals("Profesor", ignoreCase = true)
            val esPropietario = video.profesor.correo == currentEmail
            
            // Verificar permisos
            val puedeEliminar = esAdmin || (esProfesor && esPropietario)
            
            if (!puedeEliminar) {
                _mensajeEliminacion.value = "No tienes permiso para eliminar este video"
                _eliminando.value = false
                onResult(false)
                return@launch
            }
            
            val resultado = repository.eliminarVideo(video.id)
            
            if (resultado) {
                _mensajeEliminacion.value = "Video eliminado correctamente"
                obtenerVideos() // Recargar lista
            } else {
                _mensajeEliminacion.value = "Error al eliminar el video"
            }
            
            _eliminando.value = false
            onResult(resultado)
        }
    }

    fun limpiarMensaje() {
        _mensajeEliminacion.value = null
    }
}
