package com.example.educanet

import com.example.educanet.model.Cursos
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para la lista de cursos y validaciones
 */
class CursosTest {

    @Test
    fun `lista de cursos tiene 16 elementos`() {
        assertEquals(16, Cursos.lista.size)
    }

    @Test
    fun `cursos básicos están presentes`() {
        val cursosBasicos = listOf(
            "1° Básico", "2° Básico", "3° Básico", "4° Básico",
            "5° Básico", "6° Básico", "7° Básico", "8° Básico"
        )
        
        cursosBasicos.forEach { curso ->
            assertTrue("Falta curso: $curso", Cursos.lista.contains(curso))
        }
    }

    @Test
    fun `cursos medio están presentes`() {
        val cursosMedio = listOf(
            "1° Medio", "2° Medio", "3° Medio", "4° Medio"
        )
        
        cursosMedio.forEach { curso ->
            assertTrue("Falta curso: $curso", Cursos.lista.contains(curso))
        }
    }

    @Test
    fun `primer curso es 1° Básico`() {
        assertEquals("1° Básico", Cursos.lista.first())
    }

    @Test
    fun `último curso es 4° Medio`() {
        assertEquals("4° Medio", Cursos.lista.last())
    }

    @Test
    fun `no hay cursos duplicados`() {
        val cursosUnicos = Cursos.lista.toSet()
        assertEquals(Cursos.lista.size, cursosUnicos.size)
    }

    @Test
    fun `buscar curso existente`() {
        val cursoExiste = Cursos.lista.any { it == "4° Básico" }
        assertTrue(cursoExiste)
    }

    @Test
    fun `buscar curso inexistente`() {
        val cursoExiste = Cursos.lista.any { it == "9° Básico" }
        assertFalse(cursoExiste)
    }

    @Test
    fun `obtener índice de curso`() {
        val indice = Cursos.lista.indexOf("1° Medio")
        assertTrue(indice >= 0)
    }

    @Test
    fun `validar formato de cursos`() {
        Cursos.lista.forEach { curso ->
            assertTrue(
                "Formato inválido: $curso",
                curso.matches(Regex("\\d+° (Básico|Medio)"))
            )
        }
    }
}
