package com.example.educanet

import com.example.educanet.model.ClaseVirtual
import com.example.educanet.model.ProfesorSimple
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para el modelo ClaseVirtual y su funcionalidad de filtrado por curso
 */
class ClaseVirtualTest {

    @Test
    fun `crear clase virtual con todos los campos`() {
        val profesor = ProfesorSimple(
            correo = "profesor@educanet.cl",
            nombre = "María González",
            rol = "Profesor"
        )
        
        val clase = ClaseVirtual(
            id = "clase1",
            nombre = "Clase de Historia",
            nivel = "Medio",
            curso = "1° Medio",
            meet = "https://meet.google.com/abc-defg-hij",
            descripcion = "Clase sobre la Independencia de Chile",
            duracion = 45,
            profesor = profesor
        )
        
        assertEquals("clase1", clase.id)
        assertEquals("Clase de Historia", clase.nombre)
        assertEquals("1° Medio", clase.curso)
        assertEquals(45, clase.duracion)
        assertTrue(clase.meet.contains("meet.google.com"))
    }

    @Test
    fun `clase virtual sin curso debe tener curso vacío por defecto`() {
        val clase = ClaseVirtual(
            nombre = "Test Clase",
            nivel = "Básico"
        )
        
        assertEquals("", clase.curso)
    }

    @Test
    fun `filtrar clases virtuales por curso`() {
        val clases = listOf(
            ClaseVirtual(id = "1", nombre = "Clase 1", curso = "1° Medio"),
            ClaseVirtual(id = "2", nombre = "Clase 2", curso = "2° Medio"),
            ClaseVirtual(id = "3", nombre = "Clase 3", curso = "1° Medio"),
            ClaseVirtual(id = "4", nombre = "Clase 4", curso = "3° Medio")
        )
        
        val clasesFiltradas = clases.filter { it.curso == "1° Medio" }
        
        assertEquals(2, clasesFiltradas.size)
        assertTrue(clasesFiltradas.all { it.curso == "1° Medio" })
    }

    @Test
    fun `URL de Meet debe ser válida`() {
        val clase = ClaseVirtual(
            meet = "https://meet.google.com/abc-defg-hij"
        )
        
        assertTrue(clase.meet.startsWith("https://"))
    }

    @Test
    fun `clase sin URL de meet tiene campo vacío`() {
        val clase = ClaseVirtual(
            nombre = "Clase sin Meet"
        )
        
        assertEquals("", clase.meet)
    }

    @Test
    fun `duracion por defecto es cero`() {
        val clase = ClaseVirtual(nombre = "Test")
        
        assertEquals(0, clase.duracion)
    }
}
