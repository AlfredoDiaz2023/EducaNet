package com.example.educanet.ui.screens.carrito

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educanet.model.Libro
import com.example.educanet.ui.common.AppBackground
import com.example.educanet.viewmodel.CarritoViewModel

@Composable
fun CarritoScreen(
    onBack: () -> Unit,
    carritoViewModel: CarritoViewModel,
    userName: String
) {
    val uiState by carritoViewModel.uiState.collectAsState()

    // Navegar hacia atrás cuando la confirmación sea exitosa
    LaunchedEffect(uiState.confirmationSuccess) {
        if (uiState.confirmationSuccess) {
            onBack()
        }
    }

    AppBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(30.dp))
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

            Button(
                onClick = { carritoViewModel.confirmReservations(userName) },
                enabled = uiState.items.isNotEmpty() && !uiState.isConfirming,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isConfirming) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Confirmar Todas las Reservas")
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
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Quitar del carrito")
            }
        }
    }
}
