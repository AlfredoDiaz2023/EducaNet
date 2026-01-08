package com.example.educanet

import org.junit.Test
import org.junit.Assert.*

/**
 * Tests para la lógica de conexión de alumnos a clases en curso
 */
class ConexionAlumnoTest {

    data class AlumnoConectado(
        val id: String = "",
        val nombre: String = "",
        val apellido: String = "",
        val email: String = "",
        val horaConexion: Long = System.currentTimeMillis()
    )

    @Test
    fun `registrar alumno conectado`() {
        val alumno = AlumnoConectado(
            id = "alumno1",
            nombre = "Carlos",
            apellido = "González",
            email = "carlos@test.com",
            horaConexion = System.currentTimeMillis()
        )
        
        assertNotNull(alumno.id)
        assertTrue(alumno.nombre.isNotEmpty())
        assertTrue(alumno.email.isNotEmpty())
    }

    @Test
    fun `hora de conexión debe ser registrada`() {
        val tiempoAntes = System.currentTimeMillis()
        val alumno = AlumnoConectado(
            id = "alumno1",
            nombre = "Carlos",
            apellido = "González",
            email = "carlos@test.com"
        )
        val tiempoDespues = System.currentTimeMillis()
        
        assertTrue(alumno.horaConexion >= tiempoAntes)
        assertTrue(alumno.horaConexion <= tiempoDespues)
    }

    @Test
    fun `lista de alumnos conectados inicia vacía`() {
        val alumnosConectados = mutableListOf<AlumnoConectado>()
        assertTrue(alumnosConectados.isEmpty())
    }

    @Test
    fun `agregar alumno a lista de conectados`() {
        val alumnosConectados = mutableListOf<AlumnoConectado>()
        val alumno = AlumnoConectado(
            id = "alumno1",
            nombre = "Carlos",
            apellido = "González",
            email = "carlos@test.com"
        )
        
        alumnosConectados.add(alumno)
        
        assertEquals(1, alumnosConectados.size)
        assertEquals("alumno1", alumnosConectados[0].id)
    }

    @Test
    fun `evitar duplicados de alumnos conectados`() {
        val alumnosConectados = mutableMapOf<String, AlumnoConectado>()
        
        val alumno = AlumnoConectado(
            id = "alumno1",
            nombre = "Carlos",
            apellido = "González",
            email = "carlos@test.com"
        )
        
        // Intentar agregar dos veces
        alumnosConectados[alumno.id] = alumno
        alumnosConectados[alumno.id] = alumno
        
        assertEquals(1, alumnosConectados.size)
    }

    @Test
    fun `contar alumnos conectados`() {
        val alumnosConectados = mutableMapOf<String, AlumnoConectado>()
        
        alumnosConectados["a1"] = AlumnoConectado(id = "a1", nombre = "Carlos", apellido = "G", email = "c@t.com")
        alumnosConectados["a2"] = AlumnoConectado(id = "a2", nombre = "María", apellido = "L", email = "m@t.com")
        alumnosConectados["a3"] = AlumnoConectado(id = "a3", nombre = "Pedro", apellido = "S", email = "p@t.com")
        
        assertEquals(3, alumnosConectados.size)
    }

    @Test
    fun `limpiar lista de alumnos conectados al finalizar clase`() {
        val alumnosConectados = mutableMapOf<String, AlumnoConectado>()
        
        alumnosConectados["a1"] = AlumnoConectado(id = "a1", nombre = "Carlos", apellido = "G", email = "c@t.com")
        alumnosConectados["a2"] = AlumnoConectado(id = "a2", nombre = "María", apellido = "L", email = "m@t.com")
        
        alumnosConectados.clear()
        
        assertTrue(alumnosConectados.isEmpty())
    }

    @Test
    fun `verificar si alumno está conectado`() {
        val alumnosConectados = mutableMapOf<String, AlumnoConectado>()
        alumnosConectados["a1"] = AlumnoConectado(id = "a1", nombre = "Carlos", apellido = "G", email = "c@t.com")
        
        val estaConectado = alumnosConectados.containsKey("a1")
        val noEstaConectado = alumnosConectados.containsKey("a2")
        
        assertTrue(estaConectado)
        assertFalse(noEstaConectado)
    }

    @Test
    fun `calcular tiempo de conexión de alumno`() {
        val horaConexion = System.currentTimeMillis() - 60000 // Hace 1 minuto
        val alumno = AlumnoConectado(
            id = "a1",
            nombre = "Carlos",
            apellido = "G",
            email = "c@t.com",
            horaConexion = horaConexion
        )
        
        val tiempoConectado = System.currentTimeMillis() - alumno.horaConexion
        
        assertTrue(tiempoConectado >= 60000) // Al menos 1 minuto
    }

    @Test
    fun `buscar alumno por email`() {
        val alumnosConectados = listOf(
            AlumnoConectado(id = "a1", nombre = "Carlos", apellido = "G", email = "carlos@test.com"),
            AlumnoConectado(id = "a2", nombre = "María", apellido = "L", email = "maria@test.com"),
            AlumnoConectado(id = "a3", nombre = "Pedro", apellido = "S", email = "pedro@test.com")
        )
        
        val alumno = alumnosConectados.find { it.email == "maria@test.com" }
        
        assertNotNull(alumno)
        assertEquals("María", alumno?.nombre)
    }

    @Test
    fun `obtener nombre completo de alumno conectado`() {
        val alumno = AlumnoConectado(
            id = "a1",
            nombre = "Carlos",
            apellido = "González",
            email = "carlos@test.com"
        )
        
        val nombreCompleto = "${alumno.nombre} ${alumno.apellido}"
        
        assertEquals("Carlos González", nombreCompleto)
    }
}
