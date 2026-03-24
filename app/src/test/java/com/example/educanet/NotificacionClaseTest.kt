package com.example.educanet

import com.example.educanet.model.Notificacion
import com.example.educanet.model.TipoNotificacion
import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para el sistema de notificaciones de clases
 */
class NotificacionClaseTest {

    @Test
    fun `crear notificación de clase iniciada`() {
        val notificacion = Notificacion(
            id = "notif1",
            titulo = "📚 ¡Clase Iniciada!",
            mensaje = "El profesor Juan ha iniciado la clase de Matemáticas. ¡Únete ahora!",
            tipoDestinatario = "curso",
            tipoNotificacion = TipoNotificacion.CLASE_INICIADA.name,
            claseId = "clase123",
            asignatura = "Matemáticas",
            curso = "4° Básico",
            profesorNombre = "Juan Pérez",
            accionable = true,
            accionRealizada = false
        )
        
        assertEquals(TipoNotificacion.CLASE_INICIADA.name, notificacion.tipoNotificacion)
        assertEquals("curso", notificacion.tipoDestinatario)
        assertTrue(notificacion.accionable)
        assertFalse(notificacion.accionRealizada)
    }

    @Test
    fun `notificación de clase permite unirse`() {
        val notificacion = Notificacion(
            tipoNotificacion = TipoNotificacion.CLASE_INICIADA.name,
            accionable = true,
            accionRealizada = false
        )
        
        val puedeUnirse = notificacion.accionable && !notificacion.accionRealizada
        assertTrue(puedeUnirse)
    }

    @Test
    fun `notificación de clase ya unida no permite unirse de nuevo`() {
        val notificacion = Notificacion(
            tipoNotificacion = TipoNotificacion.CLASE_INICIADA.name,
            accionable = true,
            accionRealizada = true
        )
        
        val puedeUnirse = notificacion.accionable && !notificacion.accionRealizada
        assertFalse(puedeUnirse)
    }

    @Test
    fun `filtrar notificaciones para un curso específico`() {
        val notificaciones = listOf(
            Notificacion(id = "1", tipoDestinatario = "curso", curso = "4° Básico"),
            Notificacion(id = "2", tipoDestinatario = "curso", curso = "5° Básico"),
            Notificacion(id = "3", tipoDestinatario = "todos", curso = ""),
            Notificacion(id = "4", tipoDestinatario = "curso", curso = "4° Básico")
        )
        
        val cursoAlumno = "4° Básico"
        val notificacionesFiltradas = notificaciones.filter { notif ->
            notif.tipoDestinatario == "todos" || 
            (notif.tipoDestinatario == "curso" && notif.curso.equals(cursoAlumno, ignoreCase = true))
        }
        
        assertEquals(3, notificacionesFiltradas.size)
    }

    @Test
    fun `notificación para todos debe ser visible para cualquier alumno`() {
        val notificacion = Notificacion(
            tipoDestinatario = "todos",
            curso = ""
        )
        
        val esParaTodos = notificacion.tipoDestinatario == "todos"
        assertTrue(esParaTodos)
    }

    @Test
    fun `notificación de clase debe tener claseId`() {
        val notificacion = Notificacion(
            tipoNotificacion = TipoNotificacion.CLASE_INICIADA.name,
            claseId = "clase123"
        )
        
        assertTrue(notificacion.claseId.isNotEmpty())
    }

    @Test
    fun `tipos de notificación incluye CLASE_INICIADA`() {
        val tipos = TipoNotificacion.values()
        assertTrue(tipos.any { it == TipoNotificacion.CLASE_INICIADA })
    }

    @Test
    fun `verificar si notificación es de clase activa`() {
        val notificacion = Notificacion(
            tipoNotificacion = TipoNotificacion.CLASE_INICIADA.name,
            accionable = true
        )
        
        val esClaseActiva = notificacion.tipoNotificacion == TipoNotificacion.CLASE_INICIADA.name &&
                           notificacion.accionable
        assertTrue(esClaseActiva)
    }

    @Test
    fun `notificación de clase finalizada no es accionable`() {
        val notificacion = Notificacion(
            tipoNotificacion = TipoNotificacion.CLASE_INICIADA.name,
            accionable = false // Clase ya terminó
        )
        
        assertFalse(notificacion.accionable)
    }

    @Test
    fun `comparar cursos ignorando mayúsculas`() {
        val curso1 = "4° básico"
        val curso2 = "4° Básico"
        
        assertTrue(curso1.equals(curso2, ignoreCase = true))
    }
}
