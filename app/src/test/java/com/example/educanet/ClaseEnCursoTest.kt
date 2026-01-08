package com.example.educanet

import com.example.educanet.model.AlumnoConectado
import com.example.educanet.model.ClaseEnCurso
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para el sistema de clases en curso y alumnos conectados en tiempo real
 */
class ClaseEnCursoTest {

    @Test
    fun `crear clase en curso con datos completos`() {
        val ahora = System.currentTimeMillis()
        
        val clase = ClaseEnCurso(
            id = "clase123",
            asignatura = "Matemáticas",
            curso = "4° Básico",
            profesorId = "prof1",
            profesorNombre = "Juan Pérez",
            horaInicio = ahora,
            activa = true,
            duracionMinutos = 90
        )
        
        assertEquals("clase123", clase.id)
        assertEquals("Matemáticas", clase.asignatura)
        assertEquals("4° Básico", clase.curso)
        assertEquals("Juan Pérez", clase.profesorNombre)
        assertTrue(clase.activa)
        assertEquals(90, clase.duracionMinutos)
    }

    @Test
    fun `clase en curso inicia como activa`() {
        val clase = ClaseEnCurso(
            asignatura = "Lenguaje",
            curso = "5° Básico"
        )
        
        assertTrue(clase.activa)
    }

    @Test
    fun `clase finalizada tiene activa = false`() {
        val clase = ClaseEnCurso(
            asignatura = "Ciencias",
            activa = false,
            horaFin = System.currentTimeMillis()
        )
        
        assertFalse(clase.activa)
        assertNotNull(clase.horaFin)
    }

    @Test
    fun `crear alumno conectado`() {
        val ahora = System.currentTimeMillis()
        
        val alumno = AlumnoConectado(
            id = "clase1_alumno1",
            claseId = "clase1",
            alumnoId = "alumno1",
            alumnoNombre = "Pedro Soto",
            alumnoCorreo = "pedro@estudiante.cl",
            horaConexion = ahora,
            conectado = true
        )
        
        assertEquals("clase1_alumno1", alumno.id)
        assertEquals("clase1", alumno.claseId)
        assertEquals("Pedro Soto", alumno.alumnoNombre)
        assertTrue(alumno.conectado)
    }

    @Test
    fun `ID de alumno conectado sigue formato claseId_alumnoId`() {
        val claseId = "abc123"
        val alumnoId = "def456"
        val expectedId = "${claseId}_${alumnoId}"
        
        val alumno = AlumnoConectado(
            id = expectedId,
            claseId = claseId,
            alumnoId = alumnoId
        )
        
        assertEquals(expectedId, alumno.id)
        assertTrue(alumno.id.contains(claseId))
        assertTrue(alumno.id.contains(alumnoId))
    }

    @Test
    fun `filtrar alumnos conectados a una clase específica`() {
        val alumnosConectados = listOf(
            AlumnoConectado(id = "c1_a1", claseId = "clase1", alumnoId = "a1", conectado = true),
            AlumnoConectado(id = "c1_a2", claseId = "clase1", alumnoId = "a2", conectado = true),
            AlumnoConectado(id = "c2_a3", claseId = "clase2", alumnoId = "a3", conectado = true),
            AlumnoConectado(id = "c1_a4", claseId = "clase1", alumnoId = "a4", conectado = false)
        )
        
        val conectadosClase1 = alumnosConectados.filter { 
            it.claseId == "clase1" && it.conectado 
        }
        
        assertEquals(2, conectadosClase1.size)
    }

    @Test
    fun `contar alumnos conectados`() {
        val alumnosConectados = listOf(
            AlumnoConectado(claseId = "c1", conectado = true),
            AlumnoConectado(claseId = "c1", conectado = true),
            AlumnoConectado(claseId = "c1", conectado = false),
            AlumnoConectado(claseId = "c1", conectado = true)
        )
        
        val cantidadConectados = alumnosConectados.count { it.conectado }
        
        assertEquals(3, cantidadConectados)
    }

    @Test
    fun `verificar si alumno está en lista de conectados`() {
        val conectadosIds = setOf("alumno1", "alumno2", "alumno3")
        val alumnoIdBuscado = "alumno2"
        
        assertTrue(conectadosIds.contains(alumnoIdBuscado))
        assertFalse(conectadosIds.contains("alumno99"))
    }

    @Test
    fun `duracion de clase por defecto es 90 minutos`() {
        val clase = ClaseEnCurso(
            asignatura = "Test"
        )
        
        assertEquals(90, clase.duracionMinutos)
    }
}
