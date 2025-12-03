package com.example.educanet.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.util.Date

class ReservaTest : BehaviorSpec({

    given("una Reserva") {

        val fechaPrueba = Date()

        `when`("se crea con valores por defecto") {
            val reserva = Reserva()

            then("los campos deben quedar vacíos y el timestamp en null") {
                reserva.id shouldBe ""
                reserva.libroId shouldBe ""
                reserva.userId shouldBe ""
                reserva.userName shouldBe ""
                reserva.libroNombre shouldBe ""
                reserva.timestamp shouldBe null
            }
        }

        `when`("se crea con valores específicos") {
            val reserva = Reserva(
                id = "123",
                libroId = "lib001",
                userId = "usr001",
                userName = "Juan Pérez",
                libroNombre = "El Principito",
                timestamp = fechaPrueba
            )

            then("los valores deben coincidir con los entregados") {
                reserva.id shouldBe "123"
                reserva.libroId shouldBe "lib001"
                reserva.userId shouldBe "usr001"
                reserva.userName shouldBe "Juan Pérez"
                reserva.libroNombre shouldBe "El Principito"
                reserva.timestamp shouldBe fechaPrueba
            }
        }
    }
})
