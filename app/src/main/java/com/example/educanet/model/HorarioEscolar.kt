package com.example.educanet.model

/**
 * Constantes de horario escolar
 * - Cada clase dura 90 minutos
 * - 15 minutos de recreo entre clases
 * - Almuerzo de 13:00 a 14:00 (60 minutos)
 * - 15 minutos de tiempo libre después del almuerzo antes de la siguiente clase
 */
object HorarioConstantes {
    const val DURACION_CLASE_MINUTOS = 90
    const val DURACION_RECREO_MINUTOS = 15
    const val HORA_ALMUERZO_INICIO = "13:00"
    const val HORA_ALMUERZO_FIN = "14:00"
    const val DURACION_ALMUERZO_MINUTOS = 60
    const val DURACION_TIEMPO_LIBRE_POST_ALMUERZO = 15
    const val HORA_ENTRADA = "08:00"
    
    /**
     * Calcula la hora de fin dado una hora de inicio y duración en minutos
     */
    fun calcularHoraFin(horaInicio: String, duracionMinutos: Int): String {
        try {
            val parts = horaInicio.split(":")
            val totalMinutos = parts[0].toInt() * 60 + parts[1].toInt() + duracionMinutos
            val horas = totalMinutos / 60
            val minutos = totalMinutos % 60
            return String.format("%02d:%02d", horas, minutos)
        } catch (e: Exception) {
            return horaInicio
        }
    }
    
    /**
     * Genera los bloques de horario del día con las reglas establecidas
     * Ejemplo: 8:00-9:30 (Clase 1), 9:30-9:45 (Recreo), 9:45-11:15 (Clase 2), etc.
     */
    fun generarBloquesDia(): List<BloqueHorarioInfo> {
        val bloques = mutableListOf<BloqueHorarioInfo>()
        var horaActual = HORA_ENTRADA
        var numeroClase = 1
        
        while (horaActual < "17:00") {
            // Verificar si estamos en horario de almuerzo
            if (horaActual >= "12:30" && horaActual < HORA_ALMUERZO_FIN) {
                // Agregar bloque de almuerzo si no existe
                if (bloques.none { it.tipo == TipoBloque.ALMUERZO }) {
                    bloques.add(BloqueHorarioInfo(
                        horaInicio = HORA_ALMUERZO_INICIO,
                        horaFin = HORA_ALMUERZO_FIN,
                        tipo = TipoBloque.ALMUERZO,
                        numeroBloque = 0
                    ))
                }
                horaActual = HORA_ALMUERZO_FIN
                // Tiempo libre después del almuerzo
                val horaPostAlmuerzo = calcularHoraFin(HORA_ALMUERZO_FIN, DURACION_TIEMPO_LIBRE_POST_ALMUERZO)
                bloques.add(BloqueHorarioInfo(
                    horaInicio = HORA_ALMUERZO_FIN,
                    horaFin = horaPostAlmuerzo,
                    tipo = TipoBloque.TIEMPO_LIBRE,
                    numeroBloque = 0
                ))
                horaActual = horaPostAlmuerzo
                continue
            }
            
            // Agregar clase
            val horaFinClase = calcularHoraFin(horaActual, DURACION_CLASE_MINUTOS)
            
            // Si la clase terminaría después de las 12:30, ajustar para almuerzo
            if (horaActual < "12:30" && horaFinClase > "12:30") {
                // La clase termina antes del almuerzo
                bloques.add(BloqueHorarioInfo(
                    horaInicio = horaActual,
                    horaFin = horaFinClase,
                    tipo = TipoBloque.CLASE,
                    numeroBloque = numeroClase++
                ))
                horaActual = horaFinClase
                continue
            }
            
            bloques.add(BloqueHorarioInfo(
                horaInicio = horaActual,
                horaFin = horaFinClase,
                tipo = TipoBloque.CLASE,
                numeroBloque = numeroClase++
            ))
            
            // Agregar recreo después de la clase (si no es hora de almuerzo)
            val horaInicioRecreo = horaFinClase
            val horaFinRecreo = calcularHoraFin(horaFinClase, DURACION_RECREO_MINUTOS)
            
            if (horaInicioRecreo < "12:30") {
                bloques.add(BloqueHorarioInfo(
                    horaInicio = horaInicioRecreo,
                    horaFin = horaFinRecreo,
                    tipo = TipoBloque.RECREO,
                    numeroBloque = 0
                ))
                horaActual = horaFinRecreo
            } else {
                horaActual = horaFinClase
            }
            
            // Limitar a 5 clases por día
            if (numeroClase > 5) break
        }
        
        return bloques
    }
}

/**
 * Tipos de bloques en el horario
 */
enum class TipoBloque {
    CLASE,
    RECREO,
    ALMUERZO,
    TIEMPO_LIBRE
}

/**
 * Información de un bloque de horario
 */
data class BloqueHorarioInfo(
    val horaInicio: String,
    val horaFin: String,
    val tipo: TipoBloque,
    val numeroBloque: Int = 0
)

/**
 * Representa un bloque de horario para una asignatura específica
 */
data class BloqueHorario(
    val id: String = "",
    val asignatura: String = "",
    val horaInicio: String = "",  // Formato "HH:mm"
    val horaFin: String = "",     // Formato "HH:mm"
    val profesor: String = "",
    val sala: String = "",
    val duracionMinutos: Int = HorarioConstantes.DURACION_CLASE_MINUTOS
)

/**
 * Representa el horario de un día específico
 */
data class HorarioDia(
    val diaSemana: DiaSemana = DiaSemana.LUNES,
    val horaEntrada: String = "08:00",
    val horaSalida: String = "15:30",
    val bloques: List<BloqueHorario> = emptyList(),
    val bloquesInfo: List<BloqueHorarioInfo> = HorarioConstantes.generarBloquesDia()
)

/**
 * Días de la semana para el horario escolar (Jornada Escolar Completa)
 */
enum class DiaSemana(val displayName: String, val abreviatura: String) {
    LUNES("Lunes", "Lun"),
    MARTES("Martes", "Mar"),
    MIERCOLES("Miércoles", "Mié"),
    JUEVES("Jueves", "Jue"),
    VIERNES("Viernes", "Vie")
}

/**
 * Representa el horario escolar completo de un curso
 * Jornada Escolar Completa: Entrada 8:00 AM, salida variable por día
 */
data class HorarioEscolar(
    val id: String = "",
    val curso: String = "",
    val anioEscolar: Int = 2026,
    val horaEntradaGeneral: String = "08:00", // Entrada fija a las 8:00 AM
    val horarioSemanal: Map<DiaSemana, HorarioDia> = mapOf(
        DiaSemana.LUNES to HorarioDia(DiaSemana.LUNES, "08:00", "15:30"),
        DiaSemana.MARTES to HorarioDia(DiaSemana.MARTES, "08:00", "15:30"),
        DiaSemana.MIERCOLES to HorarioDia(DiaSemana.MIERCOLES, "08:00", "15:30"),
        DiaSemana.JUEVES to HorarioDia(DiaSemana.JUEVES, "08:00", "15:30"),
        DiaSemana.VIERNES to HorarioDia(DiaSemana.VIERNES, "08:00", "13:00")
    ),
    val activo: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis(),
    val fechaModificacion: Long = System.currentTimeMillis()
)

/**
 * Horarios predeterminados por tipo de jornada
 */
object HorariosPredeterminados {
    
    /**
     * Horario para Educación Básica (1° a 6° Básico)
     * Jornada Escolar Completa
     */
    val horarioBasicaPrimaria = mapOf(
        DiaSemana.LUNES to HorarioDia(DiaSemana.LUNES, "08:00", "15:30"),
        DiaSemana.MARTES to HorarioDia(DiaSemana.MARTES, "08:00", "15:30"),
        DiaSemana.MIERCOLES to HorarioDia(DiaSemana.MIERCOLES, "08:00", "15:30"),
        DiaSemana.JUEVES to HorarioDia(DiaSemana.JUEVES, "08:00", "15:30"),
        DiaSemana.VIERNES to HorarioDia(DiaSemana.VIERNES, "08:00", "13:00")
    )
    
    /**
     * Horario para Educación Básica Superior (7° y 8° Básico)
     * Jornada Escolar Completa
     */
    val horarioBasicaSuperior = mapOf(
        DiaSemana.LUNES to HorarioDia(DiaSemana.LUNES, "08:00", "16:00"),
        DiaSemana.MARTES to HorarioDia(DiaSemana.MARTES, "08:00", "16:00"),
        DiaSemana.MIERCOLES to HorarioDia(DiaSemana.MIERCOLES, "08:00", "16:00"),
        DiaSemana.JUEVES to HorarioDia(DiaSemana.JUEVES, "08:00", "16:00"),
        DiaSemana.VIERNES to HorarioDia(DiaSemana.VIERNES, "08:00", "13:30")
    )
    
    /**
     * Horario para Educación Media (1° a 4° Medio)
     * Jornada Escolar Completa
     */
    val horarioMedia = mapOf(
        DiaSemana.LUNES to HorarioDia(DiaSemana.LUNES, "08:00", "16:30"),
        DiaSemana.MARTES to HorarioDia(DiaSemana.MARTES, "08:00", "16:30"),
        DiaSemana.MIERCOLES to HorarioDia(DiaSemana.MIERCOLES, "08:00", "16:30"),
        DiaSemana.JUEVES to HorarioDia(DiaSemana.JUEVES, "08:00", "16:30"),
        DiaSemana.VIERNES to HorarioDia(DiaSemana.VIERNES, "08:00", "14:00")
    )
    
    /**
     * Obtiene el horario predeterminado según el curso
     */
    fun obtenerHorarioPredeterminado(curso: String): Map<DiaSemana, HorarioDia> {
        return when {
            curso.contains("1° Básico") || curso.contains("2° Básico") || 
            curso.contains("3° Básico") || curso.contains("4° Básico") ||
            curso.contains("5° Básico") || curso.contains("6° Básico") -> horarioBasicaPrimaria
            
            curso.contains("7° Básico") || curso.contains("8° Básico") -> horarioBasicaSuperior
            
            curso.contains("Medio") -> horarioMedia
            
            else -> horarioBasicaPrimaria
        }
    }
    
    /**
     * Calcula la duración total de clases para un día específico
     */
    fun calcularDuracionDia(horarioDia: HorarioDia): String {
        try {
            val partsInicio = horarioDia.horaEntrada.split(":")
            val partsFin = horarioDia.horaSalida.split(":")
            
            val minutosInicio = partsInicio[0].toInt() * 60 + partsInicio[1].toInt()
            val minutosFin = partsFin[0].toInt() * 60 + partsFin[1].toInt()
            
            val duracionMinutos = minutosFin - minutosInicio
            val horas = duracionMinutos / 60
            val minutos = duracionMinutos % 60
            
            return if (minutos > 0) "${horas}h ${minutos}m" else "${horas}h"
        } catch (e: Exception) {
            return "N/A"
        }
    }
}

/**
 * Generador de horarios con asignaturas predistribuidas
 * Distribuye las asignaturas de cada curso a lo largo de la semana
 */
object GeneradorHorarioConAsignaturas {
    
    /**
     * Horarios de bloques de clase estándar (90 min cada uno)
     * Bloque 1: 08:00 - 09:30
     * Bloque 2: 09:45 - 11:15
     * Bloque 3: 11:30 - 13:00
     * ALMUERZO: 13:00 - 14:00
     * Bloque 4: 14:15 - 15:45
     * Bloque 5: 16:00 - 17:30
     */
    private val bloquesHorarios = listOf(
        Pair("08:00", "09:30"),  // Bloque 1
        Pair("09:45", "11:15"),  // Bloque 2
        Pair("11:30", "13:00"),  // Bloque 3
        Pair("14:15", "15:45"),  // Bloque 4
        Pair("16:00", "17:30")   // Bloque 5
    )
    
    /**
     * Número de bloques por día según el tipo de curso
     */
    private fun obtenerBloquesPorDia(curso: String, dia: DiaSemana): Int {
        return when {
            // Básica Primaria: 3 bloques L-J, 2 bloques V
            curso.contains("1° Básico") || curso.contains("2° Básico") ||
            curso.contains("3° Básico") || curso.contains("4° Básico") ||
            curso.contains("5° Básico") || curso.contains("6° Básico") -> {
                if (dia == DiaSemana.VIERNES) 2 else 3
            }
            // Básica Superior: 4 bloques L-J, 3 bloques V
            curso.contains("7° Básico") || curso.contains("8° Básico") -> {
                if (dia == DiaSemana.VIERNES) 3 else 4
            }
            // Media: 4 bloques L-J, 3 bloques V
            curso.contains("Medio") -> {
                if (dia == DiaSemana.VIERNES) 3 else 4
            }
            else -> 3
        }
    }
    
    /**
     * Distribución de asignaturas principales (se repiten más veces en la semana)
     */
    private val asignaturasPrincipales = listOf(
        "Lenguaje y Comunicación",
        "Matemática",
        "Historia, Geografía y Ciencias Sociales",
        "Ciencias Naturales",
        "Inglés"
    )
    
    /**
     * Genera el horario completo con asignaturas para un curso específico
     */
    fun generarHorarioConAsignaturas(curso: String): Map<DiaSemana, HorarioDia> {
        val asignaturasCurso = AsignaturasPredefinidas.obtenerAsignaturasPorCurso(curso)
            .map { it.nombre }
        
        if (asignaturasCurso.isEmpty()) {
            return HorariosPredeterminados.obtenerHorarioPredeterminado(curso)
        }
        
        // Separar asignaturas principales y secundarias
        val principales = asignaturasCurso.filter { it in asignaturasPrincipales }
        val secundarias = asignaturasCurso.filter { it !in asignaturasPrincipales }
        
        // Crear distribución balanceada de asignaturas para la semana
        val distribucionSemanal = crearDistribucionSemanal(curso, principales, secundarias)
        
        val horarioBase = HorariosPredeterminados.obtenerHorarioPredeterminado(curso)
        val horarioConAsignaturas = mutableMapOf<DiaSemana, HorarioDia>()
        
        DiaSemana.entries.forEach { dia ->
            val horarioDiaBase = horarioBase[dia] ?: HorarioDia(dia, "08:00", "15:30")
            val numBloques = obtenerBloquesPorDia(curso, dia)
            val asignaturasDia = distribucionSemanal[dia] ?: emptyList()
            
            val bloques = mutableListOf<BloqueHorario>()
            
            for (i in 0 until numBloques) {
                if (i < bloquesHorarios.size && i < asignaturasDia.size) {
                    val (horaInicio, horaFin) = bloquesHorarios[i]
                    bloques.add(
                        BloqueHorario(
                            id = "${dia.name}_bloque_${i + 1}",
                            asignatura = asignaturasDia[i],
                            horaInicio = horaInicio,
                            horaFin = horaFin,
                            profesor = "",
                            sala = "Sala ${i + 1}"
                        )
                    )
                }
            }
            
            horarioConAsignaturas[dia] = horarioDiaBase.copy(bloques = bloques)
        }
        
        return horarioConAsignaturas
    }
    
    /**
     * Crea una distribución balanceada de asignaturas para toda la semana
     * - Asignaturas principales: 4-5 veces por semana
     * - Asignaturas secundarias: 1-2 veces por semana
     */
    private fun crearDistribucionSemanal(
        curso: String,
        principales: List<String>,
        secundarias: List<String>
    ): Map<DiaSemana, List<String>> {
        val distribucion = mutableMapOf<DiaSemana, MutableList<String>>()
        DiaSemana.entries.forEach { dia ->
            distribucion[dia] = mutableListOf()
        }
        
        val dias = DiaSemana.entries.toList()
        var indiceDia = 0
        
        // Asignar asignaturas principales (cada una aparece múltiples veces)
        val principalesRotacion = mutableListOf<String>()
        repeat(4) { // Cada asignatura principal aparece ~4 veces
            principalesRotacion.addAll(principales)
        }
        
        // Mezclar para variedad
        val principalesMezcladas = principalesRotacion.shuffled()
        
        // Distribuir principales en los primeros bloques de cada día
        for (asignatura in principalesMezcladas) {
            val dia = dias[indiceDia % dias.size]
            val numBloques = obtenerBloquesPorDia(curso, dia)
            
            if (distribucion[dia]!!.size < numBloques) {
                distribucion[dia]!!.add(asignatura)
            }
            indiceDia++
        }
        
        // Asignar asignaturas secundarias a los bloques restantes
        val secundariasRotacion = mutableListOf<String>()
        repeat(2) { // Cada asignatura secundaria aparece ~2 veces
            secundariasRotacion.addAll(secundarias)
        }
        
        val secundariasMezcladas = secundariasRotacion.shuffled()
        indiceDia = 0
        
        for (asignatura in secundariasMezcladas) {
            val dia = dias[indiceDia % dias.size]
            val numBloques = obtenerBloquesPorDia(curso, dia)
            
            if (distribucion[dia]!!.size < numBloques) {
                distribucion[dia]!!.add(asignatura)
            }
            indiceDia++
        }
        
        // Rellenar bloques vacíos con asignaturas principales
        dias.forEach { dia ->
            val numBloques = obtenerBloquesPorDia(curso, dia)
            while (distribucion[dia]!!.size < numBloques && principales.isNotEmpty()) {
                val asignaturaRelleno = principales[distribucion[dia]!!.size % principales.size]
                distribucion[dia]!!.add(asignaturaRelleno)
            }
        }
        
        return distribucion
    }
}
