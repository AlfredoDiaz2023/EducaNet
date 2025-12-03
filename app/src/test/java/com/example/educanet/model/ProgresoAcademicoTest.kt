package com.example.educanet.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class ProgresoAcademicoTest : BehaviorSpec({

    given("un ProgresoAcademico") {

        `when`("se crea con valores por defecto") {
            val progreso = ProgresoAcademico()

            then("los campos deben estar vacíos y notas en 0") {
                progreso.id shouldBe ""
                progreso.profesor shouldBe ""
                progreso.alumno shouldBe ""
                progreso.asignatura shouldBe ""
                progreso.curso shouldBe ""
                progreso.notas shouldBe 0.0
            }
        }

        `when`("se crea con valores específicos") {
            val progreso = ProgresoAcademico(
                id = "prog001",
                profesor = "Prof. García",
                alumno = "Juan Pérez",
                asignatura = "Matemáticas",
                curso = "8vo Básico",
                notas = 6.5
            )

            then("los valores deben coincidir con los entregados") {
                progreso.id shouldBe "prog001"
                progreso.profesor shouldBe "Prof. García"
                progreso.alumno shouldBe "Juan Pérez"
                progreso.asignatura shouldBe "Matemáticas"
                progreso.curso shouldBe "8vo Básico"
                progreso.notas shouldBe 6.5
            }
        }

        `when`("se valida una nota en rango válido (1.0 - 7.0)") {
            val progreso = ProgresoAcademico(
                id = "prog002",
                profesor = "Prof. López",
                alumno = "Ana Silva",
                asignatura = "Lenguaje",
                curso = "7mo Básico",
                notas = 5.5
            )

            then("la nota debe estar en rango válido") {
                val esValida = progreso.notas in 1.0..7.0
                esValida shouldBe true
            }
        }

        `when`("se verifica una nota perfecta") {
            val progreso = ProgresoAcademico(
                id = "prog003",
                profesor = "Prof. Martínez",
                alumno = "Pedro González",
                asignatura = "Ciencias",
                curso = "8vo Básico",
                notas = 7.0
            )

            then("la nota perfecta debe ser 7.0") {
                progreso.notas shouldBe 7.0
            }
        }

        `when`("se calcula el promedio de múltiples progresos") {
            val progresos = listOf(
                ProgresoAcademico(id = "1", profesor = "P1", alumno = "A1", asignatura = "Mat", curso = "8A", notas = 6.0),
                ProgresoAcademico(id = "2", profesor = "P2", alumno = "A1", asignatura = "Len", curso = "8A", notas = 5.0),
                ProgresoAcademico(id = "3", profesor = "P3", alumno = "A1", asignatura = "Cie", curso = "8A", notas = 7.0)
            )

            then("el promedio debe calcularse correctamente") {
                val promedio = progresos.map { it.notas }.average()
                promedio shouldBe 6.0
            }
        }

        `when`("se filtran progresos por asignatura") {
            val progresos = listOf(
                ProgresoAcademico(id = "1", profesor = "P1", alumno = "A1", asignatura = "Matemáticas", curso = "8A", notas = 6.0),
                ProgresoAcademico(id = "2", profesor = "P2", alumno = "A2", asignatura = "Lenguaje", curso = "8A", notas = 5.0),
                ProgresoAcademico(id = "3", profesor = "P1", alumno = "A3", asignatura = "Matemáticas", curso = "8B", notas = 7.0)
            )

            then("debe retornar solo los progresos de la asignatura especificada") {
                val matematicas = progresos.filter { it.asignatura == "Matemáticas" }
                matematicas.size shouldBe 2
            }
        }

        `when`("se busca la mejor nota de un alumno") {
            val progresos = listOf(
                ProgresoAcademico(id = "1", profesor = "P1", alumno = "Juan", asignatura = "Mat", curso = "8A", notas = 5.5),
                ProgresoAcademico(id = "2", profesor = "P2", alumno = "Juan", asignatura = "Len", curso = "8A", notas = 6.8),
                ProgresoAcademico(id = "3", profesor = "P3", alumno = "Juan", asignatura = "Cie", curso = "8A", notas = 6.2)
            )

            then("debe identificar la mejor nota") {
                val mejorNota = progresos.maxByOrNull { it.notas }
                mejorNota shouldNotBe null
                mejorNota?.notas shouldBe 6.8
                mejorNota?.asignatura shouldBe "Len"
            }
        }

        `when`("se copia un progreso modificando la nota") {
            val progresoOriginal = ProgresoAcademico(
                id = "prog001",
                profesor = "Prof. García",
                alumno = "Juan Pérez",
                asignatura = "Matemáticas",
                curso = "8vo Básico",
                notas = 5.0
            )
            val progresoActualizado = progresoOriginal.copy(notas = 6.5)

            then("el progreso copiado debe tener la nueva nota") {
                progresoActualizado.notas shouldBe 6.5
            }

            then("el progreso original no debe cambiar") {
                progresoOriginal.notas shouldBe 5.0
            }
        }
    }
})
