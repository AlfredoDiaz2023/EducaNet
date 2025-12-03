package com.example.educanet.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class LibroTest : BehaviorSpec({

    given("un Libro") {

        `when`("se crea con valores por defecto") {
            val libro = Libro()

            then("los campos deben estar vacíos y cantidad en 0") {
                libro.id shouldBe ""
                libro.nombre shouldBe ""
                libro.nivel shouldBe ""
                libro.imagen shouldBe ""
                libro.cantidad shouldBe 0
            }
        }

        `when`("se crea con valores específicos") {
            val libro = Libro(
                id = "lib001",
                nombre = "El Principito",
                nivel = "Básico",
                imagen = "https://ejemplo.com/imagen.jpg",
                cantidad = 5
            )

            then("los valores deben coincidir con los entregados") {
                libro.id shouldBe "lib001"
                libro.nombre shouldBe "El Principito"
                libro.nivel shouldBe "Básico"
                libro.imagen shouldBe "https://ejemplo.com/imagen.jpg"
                libro.cantidad shouldBe 5
            }
        }

        `when`("se verifica la disponibilidad de un libro con stock") {
            val libro = Libro(
                id = "lib002",
                nombre = "Don Quijote",
                nivel = "Avanzado",
                imagen = "",
                cantidad = 3
            )

            then("debe estar disponible para reservar") {
                val estaDisponible = libro.cantidad > 0
                estaDisponible shouldBe true
            }
        }

        `when`("se verifica la disponibilidad de un libro sin stock") {
            val libro = Libro(
                id = "lib003",
                nombre = "1984",
                nivel = "Intermedio",
                imagen = "",
                cantidad = 0
            )

            then("no debe estar disponible para reservar") {
                val estaDisponible = libro.cantidad > 0
                estaDisponible shouldBe false
            }
        }

        `when`("se comparan dos libros con el mismo id") {
            val libro1 = Libro(id = "lib001", nombre = "Libro A", nivel = "Básico", imagen = "", cantidad = 5)
            val libro2 = Libro(id = "lib001", nombre = "Libro A", nivel = "Básico", imagen = "", cantidad = 5)

            then("deben ser iguales") {
                libro1 shouldBe libro2
            }
        }

        `when`("se copia un libro modificando la cantidad") {
            val libroOriginal = Libro(
                id = "lib001",
                nombre = "El Principito",
                nivel = "Básico",
                imagen = "",
                cantidad = 5
            )
            val libroModificado = libroOriginal.copy(cantidad = 4)

            then("el libro copiado debe tener la nueva cantidad") {
                libroModificado.cantidad shouldBe 4
                libroModificado.nombre shouldBe libroOriginal.nombre
            }

            then("el libro original no debe cambiar") {
                libroOriginal.cantidad shouldBe 5
            }
        }
    }
})
