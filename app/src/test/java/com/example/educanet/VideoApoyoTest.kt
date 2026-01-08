package com.example.educanet

import com.example.educanet.model.ProfesorSimple
import com.example.educanet.model.VideoApoyo
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para el modelo VideoApoyo y su funcionalidad de filtrado por curso
 */
class VideoApoyoTest {

    @Test
    fun `crear video de apoyo con todos los campos`() {
        val profesor = ProfesorSimple(
            correo = "profesor@educanet.cl",
            nombre = "Juan Pérez",
            rol = "Profesor"
        )
        
        val video = VideoApoyo(
            id = "video1",
            nombre = "Introducción a las Matemáticas",
            nivel = "Básico",
            curso = "4° Básico",
            video = "https://www.youtube.com/embed/abc123",
            descripcion = "Video introductorio de matemáticas",
            duracion = 15,
            profesor = profesor
        )
        
        assertEquals("video1", video.id)
        assertEquals("Introducción a las Matemáticas", video.nombre)
        assertEquals("4° Básico", video.curso)
        assertEquals(15, video.duracion)
        assertEquals("Juan Pérez", video.profesor.nombre)
    }

    @Test
    fun `video sin curso debe tener curso vacío por defecto`() {
        val video = VideoApoyo(
            nombre = "Test Video",
            nivel = "Básico"
        )
        
        assertEquals("", video.curso)
    }

    @Test
    fun `filtrar videos por curso`() {
        val videos = listOf(
            VideoApoyo(id = "1", nombre = "Video 1", curso = "4° Básico"),
            VideoApoyo(id = "2", nombre = "Video 2", curso = "5° Básico"),
            VideoApoyo(id = "3", nombre = "Video 3", curso = "4° Básico"),
            VideoApoyo(id = "4", nombre = "Video 4", curso = "6° Básico")
        )
        
        val videosFiltrados = videos.filter { it.curso == "4° Básico" }
        
        assertEquals(2, videosFiltrados.size)
        assertTrue(videosFiltrados.all { it.curso == "4° Básico" })
    }

    @Test
    fun `video con URL de YouTube válida`() {
        val video = VideoApoyo(
            video = "https://www.youtube.com/embed/dQw4w9WgXcQ"
        )
        
        assertTrue(video.video.contains("youtube.com/embed"))
    }

    @Test
    fun `profesor simple tiene campos correctos`() {
        val profesor = ProfesorSimple(
            correo = "test@educanet.cl",
            nombre = "Test Profesor",
            rol = "Profesor"
        )
        
        assertEquals("test@educanet.cl", profesor.correo)
        assertEquals("Test Profesor", profesor.nombre)
        assertEquals("Profesor", profesor.rol)
    }
}
