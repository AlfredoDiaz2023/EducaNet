package com.example.educanet

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para validaciones de filtrado de contenido por curso
 */
class FiltradoCursoTest {

    private val cursos = listOf(
        "1° Básico", "2° Básico", "3° Básico", "4° Básico",
        "5° Básico", "6° Básico", "7° Básico", "8° Básico",
        "1° Medio", "2° Medio", "3° Medio", "4° Medio"
    )

    data class ContenidoConCurso(
        val id: String,
        val titulo: String,
        val curso: String
    )

    @Test
    fun `filtrar contenido por curso específico`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video Matemáticas", "4° Básico"),
            ContenidoConCurso("2", "Video Ciencias", "5° Básico"),
            ContenidoConCurso("3", "Video Historia", "4° Básico"),
            ContenidoConCurso("4", "Video Lenguaje", "1° Medio")
        )
        
        val cursoFiltro = "4° Básico"
        val filtrados = contenidos.filter { it.curso == cursoFiltro }
        
        assertEquals(2, filtrados.size)
        assertTrue(filtrados.all { it.curso == cursoFiltro })
    }

    @Test
    fun `sin filtro muestra todos los contenidos`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video Matemáticas", "4° Básico"),
            ContenidoConCurso("2", "Video Ciencias", "5° Básico"),
            ContenidoConCurso("3", "Video Historia", "4° Básico")
        )
        
        val cursoFiltro = "" // Sin filtro
        val filtrados = if (cursoFiltro.isEmpty()) contenidos else contenidos.filter { it.curso == cursoFiltro }
        
        assertEquals(3, filtrados.size)
    }

    @Test
    fun `filtro con curso inexistente retorna lista vacía`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video Matemáticas", "4° Básico"),
            ContenidoConCurso("2", "Video Ciencias", "5° Básico")
        )
        
        val cursoFiltro = "9° Básico"
        val filtrados = contenidos.filter { it.curso == cursoFiltro }
        
        assertTrue(filtrados.isEmpty())
    }

    @Test
    fun `verificar que curso está en lista de cursos válidos`() {
        val cursoSeleccionado = "4° Básico"
        assertTrue(cursos.contains(cursoSeleccionado))
    }

    @Test
    fun `curso inválido no está en lista`() {
        val cursoInvalido = "0° Básico"
        assertFalse(cursos.contains(cursoInvalido))
    }

    @Test
    fun `filtrar por cursos de educación básica`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video Matemáticas", "4° Básico"),
            ContenidoConCurso("2", "Video Ciencias", "5° Básico"),
            ContenidoConCurso("3", "Video Historia", "1° Medio"),
            ContenidoConCurso("4", "Video Lenguaje", "2° Medio")
        )
        
        val filtradosBasica = contenidos.filter { it.curso.contains("Básico") }
        
        assertEquals(2, filtradosBasica.size)
    }

    @Test
    fun `filtrar por cursos de educación media`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video Matemáticas", "4° Básico"),
            ContenidoConCurso("2", "Video Ciencias", "5° Básico"),
            ContenidoConCurso("3", "Video Historia", "1° Medio"),
            ContenidoConCurso("4", "Video Lenguaje", "2° Medio")
        )
        
        val filtradosMedia = contenidos.filter { it.curso.contains("Medio") }
        
        assertEquals(2, filtradosMedia.size)
    }

    @Test
    fun `contar contenidos por curso`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video Matemáticas", "4° Básico"),
            ContenidoConCurso("2", "Video Ciencias", "4° Básico"),
            ContenidoConCurso("3", "Video Historia", "4° Básico"),
            ContenidoConCurso("4", "Video Lenguaje", "1° Medio"),
            ContenidoConCurso("5", "Video Inglés", "1° Medio")
        )
        
        val conteo = contenidos.groupingBy { it.curso }.eachCount()
        
        assertEquals(3, conteo["4° Básico"])
        assertEquals(2, conteo["1° Medio"])
    }

    @Test
    fun `filtrar ignorando mayúsculas y minúsculas`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video", "4° básico"),
            ContenidoConCurso("2", "Video", "4° BÁSICO"),
            ContenidoConCurso("3", "Video", "4° Básico")
        )
        
        val cursoFiltro = "4° Básico"
        val filtrados = contenidos.filter { it.curso.equals(cursoFiltro, ignoreCase = true) }
        
        assertEquals(3, filtrados.size)
    }

    @Test
    fun `obtener cursos únicos de contenidos`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video", "4° Básico"),
            ContenidoConCurso("2", "Video", "4° Básico"),
            ContenidoConCurso("3", "Video", "5° Básico"),
            ContenidoConCurso("4", "Video", "1° Medio")
        )
        
        val cursosUnicos = contenidos.map { it.curso }.distinct()
        
        assertEquals(3, cursosUnicos.size)
        assertTrue(cursosUnicos.contains("4° Básico"))
        assertTrue(cursosUnicos.contains("5° Básico"))
        assertTrue(cursosUnicos.contains("1° Medio"))
    }

    @Test
    fun `alumno solo ve contenido de su curso`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video Mate 4", "4° Básico"),
            ContenidoConCurso("2", "Video Mate 5", "5° Básico"),
            ContenidoConCurso("3", "Video Mate 6", "6° Básico")
        )
        
        val cursoAlumno = "4° Básico"
        val contenidoParaAlumno = contenidos.filter { it.curso == cursoAlumno }
        
        assertEquals(1, contenidoParaAlumno.size)
        assertEquals("Video Mate 4", contenidoParaAlumno[0].titulo)
    }

    @Test
    fun `profesor puede ver contenido de todos los cursos`() {
        val contenidos = listOf(
            ContenidoConCurso("1", "Video", "4° Básico"),
            ContenidoConCurso("2", "Video", "5° Básico"),
            ContenidoConCurso("3", "Video", "1° Medio")
        )
        
        // El profesor no filtra por curso
        val contenidoProfesor = contenidos
        
        assertEquals(3, contenidoProfesor.size)
    }
}
