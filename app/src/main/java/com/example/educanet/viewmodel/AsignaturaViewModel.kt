package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.Asignatura
import com.example.educanet.model.Cursos
import com.example.educanet.model.NivelEducativo
import com.example.educanet.repository.AsignaturaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AsignaturaUiState(
    val asignaturas: List<Asignatura> = emptyList(),
    val asignaturaSeleccionada: Asignatura? = null,
    val asignaturasPorCurso: List<Asignatura> = emptyList(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val mostrarDialogoCrear: Boolean = false,
    val mostrarDialogoEditar: Boolean = false,
    val mostrarDialogoEliminar: Boolean = false,
    
    // Campos del formulario
    val nombre: String = "",
    val descripcion: String = "",
    val nivelEducativo: NivelEducativo = NivelEducativo.TODOS,
    val cursosSeleccionados: List<String> = emptyList(),
    val esObligatoria: Boolean = true
)

class AsignaturaViewModel : ViewModel() {
    private val repository = AsignaturaRepository()

    private val _uiState = MutableStateFlow(AsignaturaUiState())
    val uiState: StateFlow<AsignaturaUiState> = _uiState.asStateFlow()

    val cursosDisponibles = Cursos.lista
    val nivelesEducativos = NivelEducativo.entries

    init {
        cargarAsignaturas()
    }

    fun cargarAsignaturas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            
            // Primero intentar inicializar las asignaturas predefinidas si no existen
            repository.inicializarAsignaturasPredefinidas()
            
            val asignaturas = repository.obtenerTodasLasAsignaturas()
            _uiState.value = _uiState.value.copy(
                asignaturas = asignaturas,
                cargando = false
            )
            Log.d("AsignaturaVM", "Asignaturas cargadas: ${asignaturas.size}")
        }
    }

    fun cargarAsignaturasPorCurso(curso: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            val asignaturas = repository.obtenerAsignaturasPorCurso(curso)
            _uiState.value = _uiState.value.copy(
                asignaturasPorCurso = asignaturas,
                cargando = false
            )
            Log.d("AsignaturaVM", "Asignaturas para $curso: ${asignaturas.size}")
        }
    }

    // Funciones para mostrar/ocultar diálogos
    fun mostrarDialogoCrear() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoCrear = true,
            nombre = "",
            descripcion = "",
            nivelEducativo = NivelEducativo.TODOS,
            cursosSeleccionados = emptyList(),
            esObligatoria = true
        )
    }

    fun ocultarDialogoCrear() {
        _uiState.value = _uiState.value.copy(mostrarDialogoCrear = false)
        limpiarFormulario()
    }

    fun mostrarDialogoEditar(asignatura: Asignatura) {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = true,
            asignaturaSeleccionada = asignatura,
            nombre = asignatura.nombre,
            descripcion = asignatura.descripcion,
            nivelEducativo = asignatura.nivelEducativo,
            cursosSeleccionados = asignatura.cursos,
            esObligatoria = asignatura.esObligatoria
        )
    }

    fun ocultarDialogoEditar() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = false,
            asignaturaSeleccionada = null
        )
        limpiarFormulario()
    }

    fun mostrarDialogoEliminar(asignatura: Asignatura) {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEliminar = true,
            asignaturaSeleccionada = asignatura
        )
    }

    fun ocultarDialogoEliminar() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEliminar = false,
            asignaturaSeleccionada = null
        )
    }

    // Funciones para actualizar campos del formulario
    fun actualizarNombre(nombre: String) {
        _uiState.value = _uiState.value.copy(nombre = nombre)
    }

    fun actualizarDescripcion(descripcion: String) {
        _uiState.value = _uiState.value.copy(descripcion = descripcion)
    }

    fun actualizarNivelEducativo(nivel: NivelEducativo) {
        _uiState.value = _uiState.value.copy(nivelEducativo = nivel)
    }

    fun toggleCurso(curso: String) {
        val cursosActuales = _uiState.value.cursosSeleccionados.toMutableList()
        if (cursosActuales.contains(curso)) {
            cursosActuales.remove(curso)
        } else {
            cursosActuales.add(curso)
        }
        _uiState.value = _uiState.value.copy(cursosSeleccionados = cursosActuales)
    }

    fun seleccionarTodosLosCursos() {
        _uiState.value = _uiState.value.copy(cursosSeleccionados = cursosDisponibles)
    }

    fun deseleccionarTodosLosCursos() {
        _uiState.value = _uiState.value.copy(cursosSeleccionados = emptyList())
    }

    fun actualizarEsObligatoria(esObligatoria: Boolean) {
        _uiState.value = _uiState.value.copy(esObligatoria = esObligatoria)
    }

    private fun limpiarFormulario() {
        _uiState.value = _uiState.value.copy(
            nombre = "",
            descripcion = "",
            nivelEducativo = NivelEducativo.TODOS,
            cursosSeleccionados = emptyList(),
            esObligatoria = true
        )
    }

    // Operaciones CRUD
    fun crearAsignatura() {
        val state = _uiState.value
        
        if (state.nombre.isBlank()) {
            _uiState.value = state.copy(mensaje = "❌ El nombre es obligatorio")
            return
        }
        
        if (state.cursosSeleccionados.isEmpty()) {
            _uiState.value = state.copy(mensaje = "❌ Selecciona al menos un curso")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            val nuevaAsignatura = Asignatura(
                nombre = state.nombre.trim(),
                descripcion = state.descripcion.trim(),
                nivelEducativo = state.nivelEducativo,
                cursos = state.cursosSeleccionados,
                esObligatoria = state.esObligatoria
            )
            
            val exito = repository.crearAsignatura(nuevaAsignatura)
            
            _uiState.value = _uiState.value.copy(
                guardando = false,
                mensaje = if (exito) "✅ Asignatura creada correctamente" else "❌ Error al crear la asignatura",
                mostrarDialogoCrear = !exito
            )
            
            if (exito) {
                limpiarFormulario()
                cargarAsignaturas()
            }
        }
    }

    fun actualizarAsignatura() {
        val state = _uiState.value
        val asignatura = state.asignaturaSeleccionada ?: return
        
        if (state.nombre.isBlank()) {
            _uiState.value = state.copy(mensaje = "❌ El nombre es obligatorio")
            return
        }
        
        if (state.cursosSeleccionados.isEmpty()) {
            _uiState.value = state.copy(mensaje = "❌ Selecciona al menos un curso")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            val asignaturaActualizada = asignatura.copy(
                nombre = state.nombre.trim(),
                descripcion = state.descripcion.trim(),
                nivelEducativo = state.nivelEducativo,
                cursos = state.cursosSeleccionados,
                esObligatoria = state.esObligatoria
            )
            
            val exito = repository.actualizarAsignatura(asignaturaActualizada)
            
            _uiState.value = _uiState.value.copy(
                guardando = false,
                mensaje = if (exito) "✅ Asignatura actualizada correctamente" else "❌ Error al actualizar la asignatura",
                mostrarDialogoEditar = !exito
            )
            
            if (exito) {
                limpiarFormulario()
                cargarAsignaturas()
            }
        }
    }

    fun eliminarAsignatura() {
        val asignatura = _uiState.value.asignaturaSeleccionada ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            val exito = repository.eliminarAsignatura(asignatura.id)
            
            _uiState.value = _uiState.value.copy(
                guardando = false,
                mensaje = if (exito) "✅ Asignatura eliminada correctamente" else "❌ Error al eliminar la asignatura",
                mostrarDialogoEliminar = false,
                asignaturaSeleccionada = null
            )
            
            if (exito) {
                cargarAsignaturas()
            }
        }
    }

    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }

    fun refreshAsignaturas() {
        cargarAsignaturas()
    }
}
