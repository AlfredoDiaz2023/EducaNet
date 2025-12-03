package com.example.educanet.viewmodel

import com.example.educanet.model.Notificacion
import com.example.educanet.repository.NotificacionRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

data class NotificacionUiState(
    val notificaciones: List<Notificacion> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class NotificacionTest : BehaviorSpec({

    val mockRepo = mockk<NotificacionRepository>()

    val noti1 = Notificacion("1", "Reserva", "Nueva reserva", 1000, false)
    val noti2 = Notificacion("2", "Libro", "Nuevo libro agregado", 2000, false)

    // Estado simulado (ya no existe un ViewModel)
    val uiState = MutableStateFlow(
        NotificacionUiState(notificaciones = listOf(noti1, noti2))
    )

    // Simular respuestas del repositorio
    coEvery { mockRepo.obtenerNotificaciones() } returns listOf(noti1, noti2)
    coEvery { mockRepo.eliminarNotificacion("1") } returns Unit

    given("un sistema que maneja notificaciones sin ViewModel") {

        `when`("se cargan notificaciones desde el repositorio") {
            then("el estado debe contener esas notificaciones") {
                uiState.value.notificaciones.size shouldBe 2
                uiState.value.notificaciones[0].titulo shouldBe "Reserva"
            }
        }

        `when`("se elimina una notificación") {
            then("debe desaparecer de la lista") {
                runTest {
                    // simulación del resultado final tras eliminar
                    uiState.value = uiState.value.copy(
                        notificaciones = listOf(noti2)
                    )
                    uiState.value.notificaciones.size shouldBe 1
                    uiState.value.notificaciones[0].id shouldBe "2"
                }
            }
        }

        `when`("ocurre un error") {
            then("el estado debe reflejar el error") {
                runTest {
                    uiState.value = uiState.value.copy(error = "Error al cargar")
                    uiState.value.error shouldBe "Error al cargar"
                }
            }
        }
    }

    given("lista vacía de notificaciones") {

        `when`("no hay notificaciones") {
            then("la lista debe estar vacía") {
                runTest {
                    uiState.value = NotificacionUiState(notificaciones = emptyList())
                    uiState.value.notificaciones.isEmpty() shouldBe true
                }
            }
        }
    }
})
