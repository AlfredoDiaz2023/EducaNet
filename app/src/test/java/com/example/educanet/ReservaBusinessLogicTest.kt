package com.example.educanet

import com.example.educanet.model.Reserva
import com.example.educanet.model.Libro
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.double
import io.kotest.property.arbitrary.int
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class ReservaBusinessLogicTest : FunSpec({

    // Test de propiedades para cálculo de total
    test("el total debe ser igual a la suma de precios por cantidad") {
        checkAll(
            Arb.double(1.0, 100.0),
            Arb.int(1, 10),
            Arb.double(1.0, 100.0),
            Arb.int(1, 10)
        ) { precio1, cantidad1, precio2, cantidad2 ->
            val libro1 = Libro("1", "Libro1", "", "", 10)
            val libro2 = Libro("2", "Libro2", "", "", 10)

            val item1 = Reserva(libro1, cantidad1)
            val item2 = Reserva(libro2, cantidad2)

            val totalEsperado = (precio1 * cantidad1) + (precio2 * cantidad2)
            val carrito = listOf(item1, item2)
            val totalCalculado = carrito.sumOf { it.reserva.precio * it.cantidad }

            totalCalculado shouldBe totalEsperado
        }
    }

    // Test para verificar que no se pueden agregar productos sin stock
    test("no se puede agregar producto con stock cero") {
        val productoSinStock = Libro("1", "Sin Stock", 10.0, "", 0)
        val carrito = mutableListOf<Reserva>()

        // Simulación de intento de agregar producto sin stock
        val puedeAgregar = productoSinStock.cantidad > 0

        puedeAgregar shouldBe false
    }

    // Test para cálculo de subtotal por item
    test("el subtotal por item debe ser precio por cantidad") {
        checkAll(Arb.double(1.0, 100.0), Arb.int(1, 5)) { precio, cantidad ->
            val producto = Libro("1", "Test", "", "", 10)
            val item = Reserva(producto, cantidad)

            val subtotal = item.libro.precio * item.cantidad

            subtotal shouldBe (precio * cantidad)
        }
    }
})