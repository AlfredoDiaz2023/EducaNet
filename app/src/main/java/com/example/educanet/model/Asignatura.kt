package com.example.educanet.model

data class Asignatura(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val nivelEducativo: NivelEducativo = NivelEducativo.EDUCACION_BASICA,
    val cursos: List<String> = emptyList(), // Cursos en los que se imparte
    val esObligatoria: Boolean = true,
    val activa: Boolean = true,
    val fechaCreacion: Long = System.currentTimeMillis()
)

enum class NivelEducativo(val displayName: String) {
    EDUCACION_BASICA("Educación Básica"),
    EDUCACION_MEDIA("Educación Media"),
    EDUCACION_MEDIA_SUPERIOR("3° y 4° Medio"),
    TODOS("Todos los niveles")
}

/**
 * Asignaturas predefinidas del currículo chileno
 */
object AsignaturasPredefinidas {
    
    // Cursos por nivel
    val cursosBasica = listOf(
        "1° Básico", "2° Básico", "3° Básico", "4° Básico",
        "5° Básico", "6° Básico", "7° Básico", "8° Básico"
    )
    
    val cursosMedia = listOf("1° Medio", "2° Medio", "3° Medio", "4° Medio")
    
    val cursosMediaSuperior = listOf("3° Medio", "4° Medio")
    
    val todosCursos = cursosBasica + cursosMedia
    
    // Asignaturas base (todos los niveles)
    val asignaturasBase = listOf(
        Asignatura(
            nombre = "Lenguaje y Comunicación",
            descripcion = "Desarrollo de habilidades de lectura, escritura y comunicación oral",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = todosCursos,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Matemática",
            descripcion = "Desarrollo del pensamiento lógico-matemático y resolución de problemas",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = todosCursos,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Historia, Geografía y Ciencias Sociales",
            descripcion = "Comprensión del desarrollo histórico, espacial y social",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = todosCursos,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Ciencias Naturales",
            descripcion = "Estudio del mundo natural y desarrollo del pensamiento científico",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = todosCursos,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Educación Física y Salud",
            descripcion = "Desarrollo de habilidades motrices y hábitos de vida saludable",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = todosCursos,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Orientación",
            descripcion = "Desarrollo personal, social y preparación para la vida",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = todosCursos,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Tecnología",
            descripcion = "Desarrollo de habilidades tecnológicas y pensamiento computacional",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = todosCursos,
            esObligatoria = true
        )
    )
    
    // Asignaturas específicas de Educación Básica
    val asignaturasBasica = listOf(
        Asignatura(
            nombre = "Artes Visuales",
            descripcion = "Expresión artística visual y apreciación estética",
            nivelEducativo = NivelEducativo.EDUCACION_BASICA,
            cursos = cursosBasica,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Música",
            descripcion = "Expresión musical, canto, interpretación y apreciación musical",
            nivelEducativo = NivelEducativo.EDUCACION_BASICA,
            cursos = cursosBasica,
            esObligatoria = true
        )
    )
    
    // Asignaturas con inicio en 5° Básico
    val asignaturasDesde5Basico = listOf(
        Asignatura(
            nombre = "Inglés",
            descripcion = "Desarrollo de competencias comunicativas en idioma inglés",
            nivelEducativo = NivelEducativo.TODOS,
            cursos = listOf("5° Básico", "6° Básico", "7° Básico", "8° Básico") + cursosMedia,
            esObligatoria = true
        )
    )
    
    // Asignaturas específicas de Educación Media Superior (3° y 4° Medio)
    val asignaturasMediaSuperior = listOf(
        Asignatura(
            nombre = "Lengua y Literatura",
            descripcion = "Talleres de lectura y escritura avanzados, análisis literario",
            nivelEducativo = NivelEducativo.EDUCACION_MEDIA_SUPERIOR,
            cursos = cursosMediaSuperior,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Matemática Avanzada",
            descripcion = "Enfoques matemáticos avanzados: cálculo, estadística y álgebra",
            nivelEducativo = NivelEducativo.EDUCACION_MEDIA_SUPERIOR,
            cursos = cursosMediaSuperior,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Filosofía",
            descripcion = "Introducción al pensamiento filosófico, ética y lógica",
            nivelEducativo = NivelEducativo.EDUCACION_MEDIA_SUPERIOR,
            cursos = cursosMediaSuperior,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Educación Ciudadana",
            descripcion = "Formación cívica, derechos humanos y participación ciudadana",
            nivelEducativo = NivelEducativo.EDUCACION_MEDIA_SUPERIOR,
            cursos = cursosMediaSuperior,
            esObligatoria = true
        ),
        Asignatura(
            nombre = "Ciencias para la Ciudadanía",
            descripcion = "Módulos de Ambiente, Salud y Tecnología para la vida cotidiana",
            nivelEducativo = NivelEducativo.EDUCACION_MEDIA_SUPERIOR,
            cursos = cursosMediaSuperior,
            esObligatoria = true
        )
    )
    
    // Lista completa de todas las asignaturas predefinidas
    val todasLasAsignaturas = asignaturasBase + asignaturasBasica + asignaturasDesde5Basico + asignaturasMediaSuperior
    
    /**
     * Obtiene las asignaturas disponibles para un curso específico
     */
    fun obtenerAsignaturasPorCurso(curso: String): List<Asignatura> {
        return todasLasAsignaturas.filter { asignatura ->
            asignatura.cursos.contains(curso)
        }
    }
    
    /**
     * Obtiene las asignaturas por nivel educativo
     */
    fun obtenerAsignaturasPorNivel(nivel: NivelEducativo): List<Asignatura> {
        return when (nivel) {
            NivelEducativo.TODOS -> todasLasAsignaturas
            else -> todasLasAsignaturas.filter { 
                it.nivelEducativo == nivel || it.nivelEducativo == NivelEducativo.TODOS 
            }
        }
    }
}
