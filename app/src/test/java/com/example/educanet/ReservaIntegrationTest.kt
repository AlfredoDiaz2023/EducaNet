package com.example.educanet

import com.example.educanet.model.Reserva
import com.example.educanet.model.Libro
import com.example.educanet.model.ClaseVirtual
import com.example.educanet.model.ProfesorSimple
import com.example.educanet.model.ProgresoAcademico
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.util.Date

class ReservaIntegrationTest : BehaviorSpec({

    // Test de flujo completo de reservas de libros
    given("un flujo de reserva de libros") {
        val libros = listOf(
            Libro("1", "El Principito", "Básico", "img1.jpg", 5),
            Libro("2", "Don Quijote", "Avanzado", "img2.jpg", 3),
            Libro("3", "Cien Años de Soledad", "Intermedio", "img3.jpg", 0)
        )

        `when`("se filtran libros disponibles para reservar") {
            val librosDisponibles = libros.filter { it.cantidad > 0 }

            then("solo deben aparecer libros con stock mayor a 0") {
                librosDisponibles.size shouldBe 2
                librosDisponibles.map { it.nombre } shouldBe listOf("El Principito", "Don Quijote")
            }
        }

        `when`("se crea una reserva para un libro disponible") {
            val libro = libros.first()
            val reserva = Reserva(
                id = "res001",
                libroId = libro.id,
                userId = "user001",
                userName = "Juan Pérez",
                libroNombre = libro.nombre,
                timestamp = Date()
            )

            then("la reserva debe contener la información correcta") {
                reserva.libroId shouldBe "1"
                reserva.libroNombre shouldBe "El Principito"
                reserva.userName shouldBe "Juan Pérez"
                reserva.timestamp shouldNotBe null
            }
        }

        `when`("se intenta reservar un libro sin stock") {
            val libroSinStock = libros.find { it.cantidad == 0 }

            then("el libro sin stock debe ser identificado") {
                libroSinStock shouldNotBe null
                libroSinStock?.nombre shouldBe "Cien Años de Soledad"
                libroSinStock?.cantidad shouldBe 0
            }
        }
    }

    // Test de flujo de clases virtuales
    given("un flujo de gestión de clases virtuales") {
        val profesorSimple = ProfesorSimple(
            correo = "maria@educanet.com",
            nombre = "María García",
            rol = "Profesor"
        )

        val clasesVirtuales = listOf(
            ClaseVirtual(
                id = "clase001",
                nombre = "Álgebra Básica",
                profesor = profesorSimple,
                nivel = "7mo Básico",
                clase = "7A",
                descripcion = "Introducción al álgebra",
                meet = "https://meet.google.com/abc-defg-hij",
                duracion = 45
            ),
            ClaseVirtual(
                id = "clase002",
                nombre = "Geometría",
                profesor = profesorSimple,
                nivel = "8vo Básico",
                clase = "8B",
                descripcion = "Figuras geométricas",
                meet = "https://meet.google.com/xyz-uvwx-rst",
                duracion = 60
            )
        )

        `when`("se filtran clases por nivel") {
            val clases7mo = clasesVirtuales.filter { it.nivel == "7mo Básico" }

            then("debe retornar solo las clases del nivel especificado") {
                clases7mo.size shouldBe 1
                clases7mo.first().nombre shouldBe "Álgebra Básica"
            }
        }

        `when`("se verifica el link de meet de una clase") {
            val clase = clasesVirtuales.first()

            then("el link de meet debe ser válido") {
                clase.meet.startsWith("https://meet.google.com/") shouldBe true
            }
        }

        `when`("se obtiene información del profesor de una clase") {
            val clase = clasesVirtuales.first()

            then("el profesor debe estar asignado correctamente") {
                clase.profesor.nombre.isNotEmpty() shouldBe true
                clase.profesor.nombre shouldBe "María García"
                clase.profesor.rol shouldBe "Profesor"
            }
        }
    }

    // Test de flujo de progreso académico
    given("un flujo de seguimiento de progreso académico") {
        val progresos = listOf(
            ProgresoAcademico("1", "Prof. López", "Juan Pérez", "Matemáticas", "8vo A", 6.5),
            ProgresoAcademico("2", "Prof. López", "Juan Pérez", "Lenguaje", "8vo A", 5.8),
            ProgresoAcademico("3", "Prof. García", "Juan Pérez", "Ciencias", "8vo A", 6.2),
            ProgresoAcademico("4", "Prof. López", "Ana Silva", "Matemáticas", "8vo A", 7.0)
        )

        `when`("se calcula el promedio de un alumno") {
            val progresosJuan = progresos.filter { it.alumno == "Juan Pérez" }
            val promedio = progresosJuan.map { it.notas }.average()

            then("el promedio debe calcularse correctamente") {
                progresosJuan.size shouldBe 3
                promedio shouldBe 6.166666666666667
            }
        }

        `when`("se filtran progresos por asignatura") {
            val matematicas = progresos.filter { it.asignatura == "Matemáticas" }

            then("debe retornar todos los progresos de esa asignatura") {
                matematicas.size shouldBe 2
            }
        }

        `when`("se busca la mejor nota") {
            val mejorNota = progresos.maxByOrNull { it.notas }

            then("debe retornar el progreso con la nota más alta") {
                mejorNota shouldNotBe null
                mejorNota?.notas shouldBe 7.0
                mejorNota?.alumno shouldBe "Ana Silva"
            }
        }
    }

    // Test de gestión de lista de reservas
    given("una lista de reservas activas") {
        val reservas = mutableListOf(
            Reserva("r1", "lib1", "u1", "Juan", "El Principito", Date()),
            Reserva("r2", "lib2", "u2", "Ana", "Don Quijote", Date()),
            Reserva("r3", "lib3", "u1", "Juan", "1984", Date())
        )

        `when`("se eliminan reservas de un usuario específico") {
            val reservasJuan = reservas.filter { it.userName == "Juan" }

            then("debe identificar todas las reservas del usuario") {
                reservasJuan.size shouldBe 2
            }
        }

        `when`("se elimina una reserva específica") {
            reservas.removeIf { it.id == "r1" }

            then("la lista debe tener una reserva menos") {
                reservas.size shouldBe 2
                reservas.none { it.id == "r1" } shouldBe true
            }
        }
    }
})
