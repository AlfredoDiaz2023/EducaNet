package com.example.educanet

import com.example.educanet.model.Asistencia
import com.example.educanet.model.Cursos
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.doubles.shouldBeGreaterThanOrEqual
import io.kotest.matchers.doubles.shouldBeLessThanOrEqual

class AsistenciaTest : BehaviorSpec({

    // Test del modelo Asistencia
    given("un registro de asistencia") {
        val asistencia = Asistencia(
            id = "1",
            profesorId = "prof123",
            profesorNombre = "Prof. García",
            alumnoId = "alumno456",
            alumnoNombre = "Juan Pérez",
            curso = "3° Básico",
            asignaturaId = "asig123",
            asignaturaNombre = "Matemática",
            fecha = System.currentTimeMillis(),
            presente = true,
            justificacion = ""
        )

        `when`("se crea correctamente") {
            then("debe tener todos los campos correctos") {
                asistencia.id shouldBe "1"
                asistencia.profesorId shouldBe "prof123"
                asistencia.profesorNombre shouldBe "Prof. García"
                asistencia.alumnoId shouldBe "alumno456"
                asistencia.alumnoNombre shouldBe "Juan Pérez"
                asistencia.curso shouldBe "3° Básico"
                asistencia.asignaturaId shouldBe "asig123"
                asistencia.asignaturaNombre shouldBe "Matemática"
                asistencia.presente shouldBe true
                asistencia.justificacion shouldBe ""
            }
        }

        `when`("el alumno está ausente con justificación") {
            val ausente = asistencia.copy(presente = false, justificacion = "Enfermedad")
            
            then("debe marcar ausente con justificación") {
                ausente.presente shouldBe false
                ausente.justificacion shouldBe "Enfermedad"
            }
        }

        `when`("se asocia a una asignatura diferente") {
            val otraAsignatura = asistencia.copy(
                asignaturaId = "asig456",
                asignaturaNombre = "Lenguaje y Comunicación"
            )
            
            then("debe tener la nueva asignatura") {
                otraAsignatura.asignaturaId shouldBe "asig456"
                otraAsignatura.asignaturaNombre shouldBe "Lenguaje y Comunicación"
            }
        }
    }

    // Test de la lista de cursos
    given("la lista de cursos disponibles") {
        val cursos = Cursos.lista

        `when`("se verifica la lista") {
            then("debe contener 12 cursos (1° Básico a 4° Medio)") {
                cursos shouldHaveSize 12
            }

            then("debe contener cursos básicos") {
                cursos shouldContain "1° Básico"
                cursos shouldContain "4° Básico"
                cursos shouldContain "8° Básico"
            }

            then("debe contener cursos medios") {
                cursos shouldContain "1° Medio"
                cursos shouldContain "2° Medio"
                cursos shouldContain "3° Medio"
                cursos shouldContain "4° Medio"
            }
        }
    }

    // Test de cálculo de porcentaje de asistencia
    given("una lista de registros de asistencia") {
        val registros = listOf(
            Asistencia(id = "1", alumnoId = "a1", presente = true, curso = "1° Básico"),
            Asistencia(id = "2", alumnoId = "a1", presente = true, curso = "1° Básico"),
            Asistencia(id = "3", alumnoId = "a1", presente = false, curso = "1° Básico"),
            Asistencia(id = "4", alumnoId = "a1", presente = true, curso = "1° Básico"),
            Asistencia(id = "5", alumnoId = "a1", presente = true, curso = "1° Básico")
        )

        `when`("se calcula el porcentaje de asistencia") {
            val presentes = registros.count { it.presente }
            val total = registros.size
            val porcentaje = (presentes.toDouble() / total) * 100

            then("debe calcular correctamente (80%)") {
                porcentaje shouldBe 80.0
            }

            then("el porcentaje debe estar entre 0 y 100") {
                porcentaje shouldBeGreaterThanOrEqual 0.0
                porcentaje shouldBeLessThanOrEqual 100.0
            }
        }
    }

    // Test de filtrado por curso
    given("registros de asistencia de múltiples cursos") {
        val registros = listOf(
            Asistencia(id = "1", curso = "1° Básico", alumnoId = "a1"),
            Asistencia(id = "2", curso = "2° Básico", alumnoId = "a2"),
            Asistencia(id = "3", curso = "1° Básico", alumnoId = "a3"),
            Asistencia(id = "4", curso = "3° Medio", alumnoId = "a4"),
            Asistencia(id = "5", curso = "1° Básico", alumnoId = "a5")
        )

        `when`("se filtran por curso específico") {
            val filtrados = registros.filter { it.curso == "1° Básico" }

            then("debe retornar solo los del curso seleccionado") {
                filtrados shouldHaveSize 3
                filtrados.all { it.curso == "1° Básico" } shouldBe true
            }
        }
    }

    // Test de valores por defecto
    given("una asistencia con valores por defecto") {
        val asistenciaDefault = Asistencia()

        `when`("se verifica los valores por defecto") {
            then("debe tener ID vacío") {
                asistenciaDefault.id shouldBe ""
            }

            then("debe estar presente por defecto como false") {
                asistenciaDefault.presente shouldBe false
            }

            then("debe tener justificación vacía") {
                asistenciaDefault.justificacion shouldBe ""
            }

            then("debe tener curso vacío") {
                asistenciaDefault.curso shouldBe ""
            }

            then("debe tener asignaturaId vacío") {
                asistenciaDefault.asignaturaId shouldBe ""
            }

            then("debe tener asignaturaNombre vacío") {
                asistenciaDefault.asignaturaNombre shouldBe ""
            }
        }
    }

    // Test de filtrado por asignatura
    given("registros de asistencia de múltiples asignaturas") {
        val registros = listOf(
            Asistencia(id = "1", asignaturaNombre = "Matemática", alumnoId = "a1", presente = true),
            Asistencia(id = "2", asignaturaNombre = "Lenguaje", alumnoId = "a1", presente = true),
            Asistencia(id = "3", asignaturaNombre = "Matemática", alumnoId = "a1", presente = false),
            Asistencia(id = "4", asignaturaNombre = "Historia", alumnoId = "a1", presente = true),
            Asistencia(id = "5", asignaturaNombre = "Matemática", alumnoId = "a1", presente = true)
        )

        `when`("se filtran por asignatura") {
            val matematica = registros.filter { it.asignaturaNombre == "Matemática" }

            then("debe retornar solo los de la asignatura seleccionada") {
                matematica shouldHaveSize 3
                matematica.all { it.asignaturaNombre == "Matemática" } shouldBe true
            }
        }

        `when`("se calcula asistencia por asignatura") {
            val matematica = registros.filter { it.asignaturaNombre == "Matemática" }
            val presentes = matematica.count { it.presente }
            val porcentaje = (presentes.toDouble() / matematica.size) * 100

            then("debe calcular porcentaje correctamente") {
                porcentaje shouldBe (2.0 / 3.0 * 100) // 66.67%
            }
        }
    }

    // Test de agrupación por fecha
    given("registros de asistencia de diferentes fechas") {
        val hoy = System.currentTimeMillis()
        val ayer = hoy - (24 * 60 * 60 * 1000)
        
        val registros = listOf(
            Asistencia(id = "1", fecha = hoy, alumnoId = "a1"),
            Asistencia(id = "2", fecha = hoy, alumnoId = "a2"),
            Asistencia(id = "3", fecha = ayer, alumnoId = "a1"),
            Asistencia(id = "4", fecha = hoy, alumnoId = "a3")
        )

        `when`("se agrupan por fecha") {
            val agrupados = registros.groupBy { it.fecha }

            then("debe agrupar correctamente") {
                agrupados.keys.size shouldBe 2
                agrupados[hoy]?.size shouldBe 3
                agrupados[ayer]?.size shouldBe 1
            }
        }
    }

    // Test de estadísticas de asistencia
    given("estadísticas de asistencia de un curso") {
        val registros = listOf(
            Asistencia(id = "1", presente = true, alumnoId = "a1"),
            Asistencia(id = "2", presente = true, alumnoId = "a2"),
            Asistencia(id = "3", presente = false, justificacion = "Médico", alumnoId = "a3"),
            Asistencia(id = "4", presente = true, alumnoId = "a4"),
            Asistencia(id = "5", presente = false, alumnoId = "a5")
        )

        `when`("se calculan las estadísticas") {
            val total = registros.size
            val presentes = registros.count { it.presente }
            val ausentes = total - presentes
            val justificados = registros.count { !it.presente && it.justificacion.isNotEmpty() }

            then("debe calcular correctamente los presentes") {
                presentes shouldBe 3
            }

            then("debe calcular correctamente los ausentes") {
                ausentes shouldBe 2
            }

            then("debe calcular correctamente los justificados") {
                justificados shouldBe 1
            }
        }
    }
})
