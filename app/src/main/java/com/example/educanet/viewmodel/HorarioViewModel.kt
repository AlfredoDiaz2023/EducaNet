package com.example.educanet.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.educanet.model.*
import com.example.educanet.repository.HorarioRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HorarioUiState(
    val horarios: List<HorarioEscolar> = emptyList(),
    val horarioSeleccionado: HorarioEscolar? = null,
    val cursoSeleccionado: String = "",
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val mensaje: String? = null,
    val mostrarDialogoEditar: Boolean = false,
    val mostrarDialogoCrear: Boolean = false,
    val mostrarDialogoAsignaturas: Boolean = false,
    val diaEditando: DiaSemana? = null,
    val asignaturasDisponibles: List<String> = emptyList(),
    
    // Estado del formulario de edición
    val horasSalidaPorDia: Map<DiaSemana, String> = mapOf(
        DiaSemana.LUNES to "15:30",
        DiaSemana.MARTES to "15:30",
        DiaSemana.MIERCOLES to "15:30",
        DiaSemana.JUEVES to "15:30",
        DiaSemana.VIERNES to "13:00"
    ),
    
    // Bloques de asignaturas por día
    val bloquesPorDia: Map<DiaSemana, List<BloqueHorario>> = mapOf(
        DiaSemana.LUNES to emptyList(),
        DiaSemana.MARTES to emptyList(),
        DiaSemana.MIERCOLES to emptyList(),
        DiaSemana.JUEVES to emptyList(),
        DiaSemana.VIERNES to emptyList()
    )
)

class HorarioViewModel : ViewModel() {
    private val repository = HorarioRepository()
    
    private val _uiState = MutableStateFlow(HorarioUiState())
    val uiState: StateFlow<HorarioUiState> = _uiState.asStateFlow()
    
    val cursosDisponibles = Cursos.lista
    val diasSemana = DiaSemana.entries
    
    // Horas disponibles para seleccionar (desde 12:00 hasta 18:00)
    val horasSalidaDisponibles = listOf(
        "12:00", "12:30", "13:00", "13:30", "14:00", "14:30",
        "15:00", "15:30", "16:00", "16:30", "17:00", "17:30", "18:00"
    )
    
    init {
        cargarHorarios()
    }
    
    fun cargarHorarios() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            
            // Inicializar horarios predeterminados si no existen
            repository.inicializarHorariosPredeterminados()
            
            val horarios = repository.obtenerTodosLosHorarios()
            _uiState.value = _uiState.value.copy(
                horarios = horarios,
                cargando = false
            )
            Log.d("HorarioVM", "Horarios cargados: ${horarios.size}")
        }
    }
    
    fun cargarHorarioPorCurso(curso: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true, cursoSeleccionado = curso)
            
            val horario = repository.obtenerHorarioPorCurso(curso)
            if (horario != null) {
                // Verificar si el horario tiene asignaturas en los bloques
                val tieneBloques = horario.horarioSemanal.values.any { dia ->
                    dia.bloques.isNotEmpty() && dia.bloques.any { it.asignatura.isNotBlank() }
                }
                
                if (!tieneBloques) {
                    // Generar horario con asignaturas predistribuidas
                    Log.d("HorarioVM", "Horario sin asignaturas detectado, generando...")
                    val horarioConAsignaturas = GeneradorHorarioConAsignaturas.generarHorarioConAsignaturas(curso)
                    val horarioActualizado = horario.copy(
                        horarioSemanal = horarioConAsignaturas,
                        fechaModificacion = System.currentTimeMillis()
                    )
                    repository.guardarHorario(horarioActualizado)
                    
                    val horasSalida = horarioActualizado.horarioSemanal.mapValues { it.value.horaSalida }
                    _uiState.value = _uiState.value.copy(
                        horarioSeleccionado = horarioActualizado,
                        horasSalidaPorDia = horasSalida,
                        cargando = false
                    )
                    Log.d("HorarioVM", "Horario actualizado con asignaturas para $curso")
                } else {
                    val horasSalida = horario.horarioSemanal.mapValues { it.value.horaSalida }
                    _uiState.value = _uiState.value.copy(
                        horarioSeleccionado = horario,
                        horasSalidaPorDia = horasSalida,
                        cargando = false
                    )
                }
            } else {
                // Crear horario con asignaturas predistribuidas
                val horarioConAsignaturas = GeneradorHorarioConAsignaturas.generarHorarioConAsignaturas(curso)
                val horasSalida = horarioConAsignaturas.mapValues { it.value.horaSalida }
                _uiState.value = _uiState.value.copy(
                    horarioSeleccionado = HorarioEscolar(
                        curso = curso,
                        horarioSemanal = horarioConAsignaturas
                    ),
                    horasSalidaPorDia = horasSalida,
                    cargando = false
                )
            }
            Log.d("HorarioVM", "Horario cargado para $curso")
        }
    }
    
    /**
     * Reinicializa TODOS los horarios con asignaturas predistribuidas
     * Útil para actualizar horarios existentes que no tenían asignaturas
     */
    fun reinicializarTodosLosHorariosConAsignaturas() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(cargando = true)
            
            val resultado = repository.reinicializarHorariosConAsignaturas()
            
            if (resultado) {
                cargarHorarios()
                _uiState.value = _uiState.value.copy(
                    mensaje = "Horarios actualizados con asignaturas",
                    cargando = false
                )
            } else {
                _uiState.value = _uiState.value.copy(
                    mensaje = "Error al reinicializar horarios",
                    cargando = false
                )
            }
        }
    }
    
    fun seleccionarCurso(curso: String) {
        _uiState.value = _uiState.value.copy(cursoSeleccionado = curso)
        cargarHorarioPorCurso(curso)
    }
    
    fun mostrarDialogoEditar(horario: HorarioEscolar) {
        val horasSalida = horario.horarioSemanal.mapValues { it.value.horaSalida }
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = true,
            horarioSeleccionado = horario,
            cursoSeleccionado = horario.curso,
            horasSalidaPorDia = horasSalida
        )
    }
    
    fun ocultarDialogoEditar() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoEditar = false,
            horarioSeleccionado = null
        )
    }
    
    fun mostrarDialogoCrear() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoCrear = true,
            cursoSeleccionado = "",
            horasSalidaPorDia = mapOf(
                DiaSemana.LUNES to "15:30",
                DiaSemana.MARTES to "15:30",
                DiaSemana.MIERCOLES to "15:30",
                DiaSemana.JUEVES to "15:30",
                DiaSemana.VIERNES to "13:00"
            )
        )
    }
    
    fun ocultarDialogoCrear() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoCrear = false,
            cursoSeleccionado = ""
        )
    }
    
    fun actualizarHoraSalida(dia: DiaSemana, nuevaHora: String) {
        val nuevasHoras = _uiState.value.horasSalidaPorDia.toMutableMap()
        nuevasHoras[dia] = nuevaHora
        _uiState.value = _uiState.value.copy(horasSalidaPorDia = nuevasHoras)
    }
    
    fun guardarHorario() {
        val state = _uiState.value
        
        if (state.cursoSeleccionado.isEmpty()) {
            _uiState.value = state.copy(mensaje = "Debe seleccionar un curso")
            return
        }
        
        viewModelScope.launch {
            _uiState.value = state.copy(guardando = true)
            
            // Crear el horario semanal con las horas de salida configuradas
            val horarioSemanal = diasSemana.associateWith { dia ->
                HorarioDia(
                    diaSemana = dia,
                    horaEntrada = "08:00", // Entrada fija a las 8:00 AM
                    horaSalida = state.horasSalidaPorDia[dia] ?: "15:30",
                    bloques = state.horarioSeleccionado?.horarioSemanal?.get(dia)?.bloques ?: emptyList()
                )
            }
            
            val horario = HorarioEscolar(
                id = state.horarioSeleccionado?.id ?: "",
                curso = state.cursoSeleccionado,
                anioEscolar = 2026,
                horaEntradaGeneral = "08:00",
                horarioSemanal = horarioSemanal,
                activo = true
            )
            
            val exito = repository.guardarHorario(horario)
            
            if (exito) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mostrarDialogoEditar = false,
                    mostrarDialogoCrear = false,
                    mensaje = "Horario guardado exitosamente para ${state.cursoSeleccionado}"
                )
                cargarHorarios()
            } else {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Error al guardar el horario"
                )
            }
        }
    }
    
    fun eliminarHorario(horarioId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(guardando = true)
            
            val exito = repository.eliminarHorario(horarioId)
            
            if (exito) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Horario eliminado exitosamente"
                )
                cargarHorarios()
            } else {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Error al eliminar el horario"
                )
            }
        }
    }
    
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
    
    /**
     * Muestra el diálogo para editar las asignaturas de un día
     */
    fun mostrarDialogoAsignaturas(dia: DiaSemana) {
        val horario = _uiState.value.horarioSeleccionado
        val bloquesActuales = horario?.horarioSemanal?.get(dia)?.bloques ?: emptyList()
        
        _uiState.value = _uiState.value.copy(
            mostrarDialogoAsignaturas = true,
            diaEditando = dia,
            bloquesPorDia = _uiState.value.bloquesPorDia.toMutableMap().apply {
                this[dia] = bloquesActuales
            }
        )
    }
    
    fun ocultarDialogoAsignaturas() {
        _uiState.value = _uiState.value.copy(
            mostrarDialogoAsignaturas = false,
            diaEditando = null
        )
    }
    
    /**
     * Carga las asignaturas disponibles para un curso
     */
    fun cargarAsignaturasParaCurso(curso: String) {
        viewModelScope.launch {
            try {
                val snapshot = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("asignaturas")
                    .whereEqualTo("activa", true)
                    .get()
                    .addOnSuccessListener { result ->
                        val asignaturas = result.documents.mapNotNull { doc ->
                            val cursos = doc.get("cursos") as? List<*>
                            val nombre = doc.getString("nombre") ?: ""
                            if (cursos?.contains(curso) == true && nombre.isNotEmpty()) {
                                nombre
                            } else null
                        }
                        _uiState.value = _uiState.value.copy(
                            asignaturasDisponibles = asignaturas.distinct()
                        )
                    }
            } catch (e: Exception) {
                Log.e("HorarioVM", "Error cargando asignaturas", e)
            }
        }
    }
    
    /**
     * Agrega un bloque de asignatura a un día
     */
    fun agregarBloqueAsignatura(dia: DiaSemana, asignatura: String, profesor: String, sala: String) {
        val bloquesActuales = _uiState.value.bloquesPorDia[dia]?.toMutableList() ?: mutableListOf()
        val numeroBloques = bloquesActuales.size
        
        // Calcular hora de inicio basada en los bloques existentes
        val bloquesInfo = HorarioConstantes.generarBloquesDia()
        val bloqueInfo = bloquesInfo.filter { it.tipo == TipoBloque.CLASE }.getOrNull(numeroBloques)
        
        val nuevoBloque = BloqueHorario(
            id = java.util.UUID.randomUUID().toString(),
            asignatura = asignatura,
            horaInicio = bloqueInfo?.horaInicio ?: "08:00",
            horaFin = bloqueInfo?.horaFin ?: "09:30",
            profesor = profesor,
            sala = sala,
            duracionMinutos = HorarioConstantes.DURACION_CLASE_MINUTOS
        )
        
        bloquesActuales.add(nuevoBloque)
        
        val nuevosBloques = _uiState.value.bloquesPorDia.toMutableMap()
        nuevosBloques[dia] = bloquesActuales
        
        _uiState.value = _uiState.value.copy(bloquesPorDia = nuevosBloques)
    }
    
    /**
     * Elimina un bloque de asignatura de un día
     */
    fun eliminarBloqueAsignatura(dia: DiaSemana, bloqueId: String) {
        val bloquesActuales = _uiState.value.bloquesPorDia[dia]?.toMutableList() ?: return
        bloquesActuales.removeAll { it.id == bloqueId }
        
        // Recalcular horarios de los bloques restantes
        val bloquesInfo = HorarioConstantes.generarBloquesDia().filter { it.tipo == TipoBloque.CLASE }
        val bloquesActualizados = bloquesActuales.mapIndexed { index, bloque ->
            val info = bloquesInfo.getOrNull(index)
            bloque.copy(
                horaInicio = info?.horaInicio ?: bloque.horaInicio,
                horaFin = info?.horaFin ?: bloque.horaFin
            )
        }
        
        val nuevosBloques = _uiState.value.bloquesPorDia.toMutableMap()
        nuevosBloques[dia] = bloquesActualizados
        
        _uiState.value = _uiState.value.copy(bloquesPorDia = nuevosBloques)
    }
    
    /**
     * Guarda los bloques de asignaturas para un día en el horario
     */
    fun guardarBloquesDelDia(dia: DiaSemana) {
        val state = _uiState.value
        val horarioActual = state.horarioSeleccionado ?: return
        
        viewModelScope.launch {
            _uiState.value = state.copy(guardando = true)
            
            val bloquesDelDia = state.bloquesPorDia[dia] ?: emptyList()
            
            // Actualizar el horario del día con los nuevos bloques
            val horarioSemanalActualizado = horarioActual.horarioSemanal.toMutableMap()
            val horarioDiaActual = horarioSemanalActualizado[dia] ?: HorarioDia(dia)
            horarioSemanalActualizado[dia] = horarioDiaActual.copy(bloques = bloquesDelDia)
            
            val horarioActualizado = horarioActual.copy(
                horarioSemanal = horarioSemanalActualizado,
                fechaModificacion = System.currentTimeMillis()
            )
            
            val exito = repository.guardarHorario(horarioActualizado)
            
            if (exito) {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mostrarDialogoAsignaturas = false,
                    diaEditando = null,
                    horarioSeleccionado = horarioActualizado,
                    mensaje = "Asignaturas guardadas para ${dia.displayName}"
                )
                cargarHorarios()
            } else {
                _uiState.value = _uiState.value.copy(
                    guardando = false,
                    mensaje = "Error al guardar las asignaturas"
                )
            }
        }
    }
    
    /**
     * Obtiene la duración de la jornada para un día específico
     */
    fun obtenerDuracionJornada(dia: DiaSemana): String {
        val horaSalida = _uiState.value.horasSalidaPorDia[dia] ?: "15:30"
        val horarioDia = HorarioDia(dia, "08:00", horaSalida)
        return HorariosPredeterminados.calcularDuracionDia(horarioDia)
    }
    
    /**
     * Obtiene los cursos que aún no tienen horario configurado
     */
    fun obtenerCursosSinHorario(): List<String> {
        val cursosConHorario = _uiState.value.horarios.map { it.curso }.toSet()
        return cursosDisponibles.filter { it !in cursosConHorario }
    }
}
