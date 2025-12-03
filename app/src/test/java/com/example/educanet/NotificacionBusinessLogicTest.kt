package com.example.educanet

import com.example.educanet.model.Notificacion
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll

class NotificacionBusinessLogicTest : FunSpec({

    test("una notificación no leída debe tener isRead = false") {
        val noti = Notificacion(
            id = "n1",
            titulo = "Prueba",
            mensaje = "Mensaje",
            isRead = false
        )
        noti.isRead shouldBe false
    }

    test("una notificación marcada como leída debe tener isRead = true") {
        val noti = Notificacion(
            id = "n2",
            titulo = "Noticia",
            mensaje = "Algo pasó",
            isRead = true
        )
        noti.isRead shouldBe true
    }

    test("título y mensaje nunca deben ser nulos") {
        checkAll(Arb.string(1..20), Arb.string(1..50)) { t, m ->
            val noti = Notificacion(titulo = t, mensaje = m)
            noti.titulo.isNotEmpty() shouldBe true
            noti.mensaje.isNotEmpty() shouldBe true
        }
    }
})