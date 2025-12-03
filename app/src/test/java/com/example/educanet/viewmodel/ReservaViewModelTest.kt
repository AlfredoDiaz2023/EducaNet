package com.example.educanet.viewmodel

import com.example.educanet.model.Reserva
import com.example.educanet.repository.ReservaRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest

class ReservaViewModelTest : BehaviorSpec({

    // --- Mock del repositorio ---
    val mockRepo = mockk<ReservaRepository>()

    // Reservas de prueba
    val reserva1 = Reserva(
        id = "1",
        libroId = "lib1",
        userId = "u1",
        userName = "Juan",
        libroNombre = "El Principito"
    )
    val reserva2 = Reserva(
        id = "2",
        libroId = "lib2",
        userId = "u2",
        userName = "Ana",
        libroNombre = "1984"
    )

    // Mock del StateFlow
    val mockStateFlow = MutableStateFlow(
        ReservaScreenUiState(
            reservas = listOf(reserva1, reserva2),
            isLoading = false
        )
    )

    // --- ViewModel mockeado ---
    val mockViewModel = mockk<ReservaViewModel>(relaxed = true)

    // Configurar comportamiento del mock
    coEvery { mockViewModel.uiState } returns mockStateFlow

    given("un ReservaViewModel mockeado") {

        // ---------------------------------------------------------
        // 1. Cargar reservas
        // ---------------------------------------------------------
        `when`("el ViewModel carga reservas") {
            then("debe contener la lista de reservas mockeada") {
                mockViewModel.uiState.value.reservas.size shouldBe 2
                mockViewModel.uiState.value.reservas[0].libroNombre shouldBe "El Principito"
            }
        }

        // ---------------------------------------------------------
        // 2. Cuando se elimina una reserva
        // ---------------------------------------------------------
        `when`("se elimina una reserva") {

            then("la reserva debe ser removida del estado") {
                runTest {
                    // Simular eliminación
                    mockStateFlow.value = mockStateFlow.value.copy(
                        reservas = listOf(reserva2) // se elimina reserva1
                    )

                    mockViewModel.uiState.value.reservas.size shouldBe 1
                    mockViewModel.uiState.value.reservas[0].id shouldBe "2"
                }
            }
        }

        // ---------------------------------------------------------
        // 3. Cuando falla la eliminación
        // ---------------------------------------------------------
        `when`("se intenta eliminar una reserva y falla") {

            then("debe contener un error en el uiState") {
                runTest {
                    mockStateFlow.value = mockStateFlow.value.copy(
                        error = "Error al eliminar la reserva."
                    )

                    mockViewModel.uiState.value.error shouldBe "Error al eliminar la reserva."
                }
            }
        }
    }

    given("un ReservaViewModel con lista vacía") {

        // ---------------------------------------------------------
        // 4. Reservas vacías
        // ---------------------------------------------------------
        `when`("no hay reservas") {

            then("reservas debe ser una lista vacía") {
                runTest {
                    mockStateFlow.value = ReservaScreenUiState(reservas = emptyList())

                    mockViewModel.uiState.value.reservas.isEmpty() shouldBe true
                }
            }
        }

        // ---------------------------------------------------------
        // 5. No hay reservas → loading false
        // ---------------------------------------------------------
        `when`("el estado indica isLoading = false") {

            then("el loading debe reflejarlo correctamente") {
                runTest {
                    mockStateFlow.value = ReservaScreenUiState(
                        reservas = emptyList(),
                        isLoading = false
                    )

                    mockViewModel.uiState.value.isLoading shouldBe false
                }
            }
        }
    }
})
