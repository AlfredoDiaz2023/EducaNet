package com.example.educanet

import com.example.educanet.model.Reserva
import com.example.educanet.model.Libro
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class ReservaIntegrationTest : BehaviorSpec({

    // Test de flujo completo del carrito
    given("un flujo de compra completo") {
        val libros = listOf(
            Libro("1", "Laptop", 1000.0, "img1.jpg", 5),
            Libro("2", "Mouse", 25.0, "img2.jpg", 10),
            Libro("3", "Teclado", 75.0, "img3.jpg", 8)
        )

        `when`("se agregan múltiples libros a las reservas") {
            val carrito = mutableListOf<Reserva>()
            libros.forEach { libro ->
                carrito.add(Reserva(libro, 1))
            }

            then("Las reservas deben contener todos los libros") {
                carrito.size shouldBe 3
                carrito.map { it.libro.nombre } shouldBe listOf("Laptop", "Mouse", "Teclado")
            }

            then("el total debe ser la suma de todos los libros") {
                val total = carrito.sumOf { it.libro.precio * it.cantidad }
                total shouldBe 1100.0
            }
        }

        `when`("se modifica la cantidad de libros") {
            val reserva = mutableListOf<Reserva>()
            val libro = libros.first()
            reserva.add(Reserva(libro, 2)) // Cantidad 2

            then("el subtotal debe reflejar la nueva cantidad") {
                val subtotal = reserva.first().libro.precio * reserva.first().cantidad
                subtotal shouldBe 2000.0
            }
        }

        `when`("se vacía las reservas") {
            val reserva = mutableListOf<Reserva>()
            libros.forEach { libro ->
                reserva.add(Reserva(libro, 1))
            }
            reserva.clear()

            then("La reserva debe estar vacía") {
                reserva.isEmpty() shouldBe true
            }
        }
    }
})
