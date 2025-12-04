package com.example.educanet.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class ClaseVirtualTest : BehaviorSpec({

    given("una ClaseVirtual") {

        `when`("se crea con valores por defecto") {
            val clase = ClaseVirtual()

            then("los campos deben estar vacíos y duración en 0") {
                clase.id shouldBe ""
                clase.nombre shouldBe ""
                clase.profesor.nombre shouldBe ""
                clase.nivel shouldBe ""
                clase.clase shouldBe ""
                clase.descripcion shouldBe ""
                clase.meet shouldBe ""
                clase.duracion shouldBe 0
            }
        }

        `when`("se crea con un profesor asignado") {
            val profesorSimple = ProfesorSimple(
                correo = "maria@educanet.com",
                nombre = "María García",
                rol = "Profesor"
            )

            val clase = ClaseVirtual(
                id = "clase001",
                nombre = "Álgebra Básica",
                profesor = profesorSimple,
                nivel = "7mo Básico",
                clase = "7A",
                descripcion = "Introducción al álgebra",
                meet = "https://meet.google.com/abc-defg-hij",
                duracion = 45
            )

            then("la clase debe tener el profesor asignado") {
                clase.profesor.nombre.isNotEmpty() shouldBe true
                clase.profesor.nombre shouldBe "María García"
            }

            then("los demás valores deben coincidir") {
                clase.nombre shouldBe "Álgebra Básica"
                clase.nivel shouldBe "7mo Básico"
                clase.duracion shouldBe 45
            }
        }

        `when`("se verifica el link de Google Meet") {
            val clase = ClaseVirtual(
                id = "clase002",
                nombre = "Geometría",
                profesor = ProfesorSimple(),
                nivel = "8vo Básico",
                clase = "8B",
                descripcion = "Figuras geométricas",
                meet = "https://meet.google.com/xyz-uvwx-rst",
                duracion = 60
            )

            then("el link debe tener el formato correcto de Google Meet") {
                clase.meet.startsWith("https://meet.google.com/") shouldBe true
            }
        }

        `when`("se crea una clase sin link de meet") {
            val clase = ClaseVirtual(
                id = "clase003",
                nombre = "Historia",
                profesor = ProfesorSimple(),
                nivel = "6to Básico",
                clase = "6A",
                descripcion = "Historia de Chile",
                meet = "",
                duracion = 45
            )

            then("el link de meet debe estar vacío") {
                clase.meet.isEmpty() shouldBe true
            }
        }

        `when`("se filtran clases por nivel") {
            val clases = listOf(
                ClaseVirtual(id = "1", nombre = "Matemáticas 7mo", profesor = ProfesorSimple(), nivel = "7mo Básico", clase = "7A", descripcion = "", meet = "", duracion = 45),
                ClaseVirtual(id = "2", nombre = "Lenguaje 7mo", profesor = ProfesorSimple(), nivel = "7mo Básico", clase = "7B", descripcion = "", meet = "", duracion = 45),
                ClaseVirtual(id = "3", nombre = "Matemáticas 8vo", profesor = ProfesorSimple(), nivel = "8vo Básico", clase = "8A", descripcion = "", meet = "", duracion = 45)
            )

            then("debe retornar solo las clases del nivel especificado") {
                val clases7mo = clases.filter { it.nivel == "7mo Básico" }
                clases7mo.size shouldBe 2
            }
        }

        `when`("se verifica la duración de clases") {
            val clases = listOf(
                ClaseVirtual(id = "1", nombre = "Clase corta", profesor = ProfesorSimple(), nivel = "", clase = "", descripcion = "", meet = "", duracion = 30),
                ClaseVirtual(id = "2", nombre = "Clase normal", profesor = ProfesorSimple(), nivel = "", clase = "", descripcion = "", meet = "", duracion = 45),
                ClaseVirtual(id = "3", nombre = "Clase larga", profesor = ProfesorSimple(), nivel = "", clase = "", descripcion = "", meet = "", duracion = 90)
            )

            then("debe identificar clases con duración mayor a 60 minutos") {
                val clasesLargas = clases.filter { it.duracion > 60 }
                clasesLargas.size shouldBe 1
                clasesLargas.first().nombre shouldBe "Clase larga"
            }
        }

        `when`("se copia una clase modificando la duración") {
            val claseOriginal = ClaseVirtual(
                id = "clase001",
                nombre = "Álgebra",
                profesor = ProfesorSimple(),
                nivel = "7mo Básico",
                clase = "7A",
                descripcion = "Clase de álgebra",
                meet = "https://meet.google.com/abc",
                duracion = 45
            )
            val claseModificada = claseOriginal.copy(duracion = 60)

            then("la clase copiada debe tener la nueva duración") {
                claseModificada.duracion shouldBe 60
            }

            then("la clase original no debe cambiar") {
                claseOriginal.duracion shouldBe 45
            }
        }
    }
})
