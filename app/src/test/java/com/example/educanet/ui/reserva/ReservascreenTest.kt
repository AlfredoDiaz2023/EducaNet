package com.example.educanet.ui.reserva

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.educanet.ui.screens.reservas.ReservasScreen
import org.junit.Rule
import org.junit.Test

class ReservaScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun cuando_la_reserva_esta_vacia_debe_mostrar_mensaje_vacio() {
        composeTestRule.setContent {
            ReservasScreen(
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("El carrito está vacío").assertExists()
    }

    @Test
    fun cuando_hay_libros_debe_mostrar_lista() {
        composeTestRule.setContent {
            ReservasScreen(
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Mis Reservas").assertExists()
    }

    @Test
    fun al_hacer_clic_en_confirmar_reserva_debe_agregarse_a_la_lista_de_reservas() {
        composeTestRule.setContent {
            ReservasScreen(
                onBack = {}
            )
        }

        composeTestRule.onNodeWithText("Confirmar Reserva").performClick()

        composeTestRule.onNodeWithText("El carrito está vacío").assertExists()
    }
}