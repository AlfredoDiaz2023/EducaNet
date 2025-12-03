package com.example.educanet.model

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

class NotificacionTest : BehaviorSpec({

    given("una Notificacion") {

        `when`("se crea con valores por defecto") {
            val noti = Notificacion()

            then("los campos deben quedar vacíos y isRead en false") {
                noti.id shouldBe ""
                noti.titulo shouldBe ""
                noti.mensaje shouldBe ""
                noti.isRead shouldBe false
                // fecha tiene valor por defecto de System.currentTimeMillis(), así que solo verificamos que sea mayor a 0
                (noti.fecha > 0) shouldBe true
            }
        }

        `when`("se crea con valores específicos") {
            val noti = Notificacion(
                id = "n001",
                titulo = "Nueva Reserva",
                mensaje = "Has reservado El Principito",
                fecha = 123456789L,
                isRead = true
            )

            then("los valores deben coincidir con los entregados") {
                noti.id shouldBe "n001"
                noti.titulo shouldBe "Nueva Reserva"
                noti.mensaje shouldBe "Has reservado El Principito"
                noti.fecha shouldBe 123456789L
                noti.isRead shouldBe true
            }
        }
    }
})