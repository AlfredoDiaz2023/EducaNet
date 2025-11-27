package com.example.educanet.ui.screens.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educanet.model.Libro
import com.example.educanet.repository.CarritoRepository
import com.example.educanet.ui.common.AppBackground
import com.example.educanet.viewmodel.CarritoViewModel
import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarritoScreen(
    onBack: () -> Unit,
    carritoViewModel: CarritoViewModel,
    userName: String
) {
    val uiState by carritoViewModel.uiState.collectAsState()
    var isRequesting by remember { mutableStateOf(false) }

    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.confirmationMessage) {
        uiState.confirmationMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            carritoViewModel.messageShown()   // ✔ limpiar mensaje
        }
    }

    // Cuando termine la confirmación, volver atrás
    LaunchedEffect(uiState.confirmationSuccess) {
        if (uiState.confirmationSuccess) {
            onBack()
            carritoViewModel.messageShown()   // ✔ limpiar success (esta es la función correcta)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->

        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(padding)
            ) {

                Spacer(modifier = Modifier.height(30.dp))

                // Encabezado
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reservas", style = MaterialTheme.typography.headlineSmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista o mensaje vacío
                if (uiState.items.isEmpty()) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aún no has seleccionado libros para reservar.")
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(uiState.items, key = { it.id }) { libro ->
                            CarritoItem(
                                libro = libro,
                                onRemove = { carritoViewModel.removeFromCart(libro) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botón confirmar reservas
                val scope = rememberCoroutineScope()

                Button(
                    onClick = {
                        scope.launch {
                            isRequesting = true
                            try {
                                carritoViewModel.confirmReservations(userName)

                                // ⬇️ MOSTRAR MENSAJE DE RESERVA EXITOSA
                                snackbarHostState.showSnackbar(
                                    message = "¡Reserva exitosa!",
                                    duration = SnackbarDuration.Short
                                )
                            } finally {
                                isRequesting = false
                            }
                        }
                    },
                    enabled = uiState.items.isNotEmpty() && !uiState.isConfirming,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3881EC),
                        contentColor = Color.White
                    )
                ) {
                    if (uiState.isConfirming) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Confirmar Todas las Reservas")
                    }
                }
            }
        }
    }
}

@Composable
fun CarritoItem(
    libro: Libro,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(libro.nombre, fontWeight = FontWeight.Bold)
                Text("Nivel: ${libro.nivel}")

                // AGREGAR ESTO PARA VER LIBRO.cantidad EN TIEMPO REAL
                Text("Stock: ${libro.cantidad}")
            }

            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar del carrito")
            }
        }
    }
}

