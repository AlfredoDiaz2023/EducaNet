package com.example.educanet.model

/**
 * Sistema de calificación chileno
 * Escala de notas: 1.0 a 7.0
 * Nota mínima aprobatoria: 4.0
 */
object SistemaNotas {
    
    // Nota mínima y máxima
    const val NOTA_MINIMA = 1.0
    const val NOTA_MAXIMA = 7.0
    const val NOTA_APROBACION = 4.0
    const val NOTA_APROBATORIA = 4.0 // Alias para compatibilidad
    
    // Tabla de conversión porcentaje a nota (escala chilena 60%)
    // Con 60% de exigencia: 60% del puntaje = 4.0
    val tablaConversion = listOf(
        RangoNota(0, 9, 1.0, "Muy Deficiente"),
        RangoNota(10, 19, 1.5, "Muy Deficiente"),
        RangoNota(20, 29, 2.0, "Deficiente"),
        RangoNota(30, 39, 2.5, "Deficiente"),
        RangoNota(40, 49, 3.0, "Insuficiente"),
        RangoNota(50, 54, 3.5, "Insuficiente"),
        RangoNota(55, 59, 3.9, "Insuficiente"),
        RangoNota(60, 64, 4.0, "Suficiente"),
        RangoNota(65, 69, 4.5, "Suficiente"),
        RangoNota(70, 74, 5.0, "Bueno"),
        RangoNota(75, 79, 5.5, "Bueno"),
        RangoNota(80, 84, 6.0, "Muy Bueno"),
        RangoNota(85, 89, 6.5, "Muy Bueno"),
        RangoNota(90, 94, 6.8, "Excelente"),
        RangoNota(95, 100, 7.0, "Excelente")
    )
    
    /**
     * Convierte un porcentaje (0-100) a una nota (1.0-7.0)
     */
    fun porcentajeANota(porcentaje: Int): Double {
        val porcentajeAjustado = porcentaje.coerceIn(0, 100)
        return tablaConversion.find { 
            porcentajeAjustado in it.porcentajeMin..it.porcentajeMax 
        }?.nota ?: 1.0
    }
    
    /**
     * Convierte un porcentaje Double (0.0-100.0) a una nota (1.0-7.0)
     */
    fun porcentajeANota(porcentaje: Double): Double {
        return porcentajeANota(porcentaje.toInt())
    }
    
    /**
     * Convierte una nota (1.0-7.0) a un porcentaje aproximado
     */
    fun notaAPorcentaje(nota: Double): Int {
        val notaAjustada = nota.coerceIn(NOTA_MINIMA, NOTA_MAXIMA)
        return tablaConversion.find { 
            notaAjustada <= it.nota 
        }?.porcentajeMin ?: 0
    }
    
    /**
     * Obtiene la descripción de una nota
     */
    fun obtenerDescripcion(nota: Double): String {
        return when {
            nota >= 6.5 -> "Excelente"
            nota >= 5.5 -> "Muy Bueno"
            nota >= 4.5 -> "Bueno"
            nota >= 4.0 -> "Suficiente"
            nota >= 3.0 -> "Insuficiente"
            nota >= 2.0 -> "Deficiente"
            else -> "Muy Deficiente"
        }
    }
    
    /**
     * Alias para obtenerDescripcion
     */
    fun getDescripcion(nota: Double): String = obtenerDescripcion(nota)
    
    /**
     * Verifica si una nota es aprobatoria
     */
    fun esAprobado(nota: Double): Boolean {
        return nota >= NOTA_APROBACION
    }
    
    /**
     * Obtiene el color asociado a una nota
     */
    fun obtenerColorHex(nota: Double): Long {
        return when {
            nota >= 6.0 -> 0xFF4CAF50 // Verde - Excelente
            nota >= 5.0 -> 0xFF8BC34A // Verde claro - Bueno
            nota >= 4.0 -> 0xFFFF9800 // Naranja - Suficiente
            nota >= 3.0 -> 0xFFFF5722 // Naranja oscuro - Insuficiente
            else -> 0xFFF44336 // Rojo - Deficiente
        }
    }
    
    /**
     * Valida que una nota esté en el rango válido
     */
    fun esNotaValida(nota: Double): Boolean {
        return nota in NOTA_MINIMA..NOTA_MAXIMA
    }
    
    /**
     * Calcula nota con puntaje obtenido y puntaje total
     * Usando escala de 60% de exigencia
     */
    fun calcularNota(puntajeObtenido: Double, puntajeTotal: Double): Double {
        if (puntajeTotal <= 0) return NOTA_MINIMA
        val porcentaje = ((puntajeObtenido / puntajeTotal) * 100).toInt()
        return porcentajeANota(porcentaje)
    }
}

/**
 * Clase que representa un rango de porcentaje y su nota equivalente
 */
data class RangoNota(
    val porcentajeMin: Int,
    val porcentajeMax: Int,
    val nota: Double,
    val descripcion: String
)
