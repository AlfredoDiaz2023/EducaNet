package com.example.educanet.ui.screens.reservas

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.model.Reserva
import com.example.educanet.ui.common.AppBackground
import com.example.educanet.viewmodel.ReservaViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReservasScreen(
    onBack: () -> Unit,
    viewModel: ReservaViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Auto-refresh cada 15 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000) // 15 segundos
            viewModel.refreshReservas()
        }
    }

    // Muestra el snackbar para mensajes de confirmación o de error
    LaunchedEffect(uiState.confirmationMessage, uiState.error) {
        uiState.confirmationMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.messageShown() // Resetea el mensaje después de mostrarlo
            }
        }
        uiState.error?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                // Idealmente, también tendrías una función para limpiar el error en el ViewModel
            }
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) {
        AppBackground {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reservas de Libros", style = MaterialTheme.typography.headlineSmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(uiState.reservas, key = { it.id }) {
                            ReservaItem(reserva = it, onEliminar = { viewModel.eliminarReserva(it.id) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReservaItem(
    reserva: Reserva,
    onEliminar: () -> Unit
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
                Text("Libro: ${reserva.libroNombre}", fontWeight = FontWeight.Bold)
                Text("Usuario: ${reserva.userName}")
            }
            IconButton(onClick = onEliminar) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar Reserva")
            }
        }
    }
}
