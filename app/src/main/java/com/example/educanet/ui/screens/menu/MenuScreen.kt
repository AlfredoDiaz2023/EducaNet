package com.example.educanet.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.viewmodel.CarritoViewModel
import com.example.educanet.model.Libro

@Composable
fun MenuScreen(
    nombre: String,
    rol: String,
    onVerCarrito: () -> Unit,
    onLogout: () -> Unit,
    viewModel: CarritoViewModel = viewModel()
) {
    val libros by viewModel.libros.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val carrito by viewModel.carrito.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Bienvenido, $nombre", fontWeight = FontWeight.Bold)
                Text("Rol: $rol", fontSize = 14.sp)
            }

            // Botón del carrito con badge
            BadgedBox(
                badge = {
                    if (carrito.isNotEmpty()) {
                        Badge {
                            Text(carrito.sumOf { it.cantidad }.toString())
                        }
                    }
                }
            ) {
                IconButton(onClick = onVerCarrito) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = "Carrito")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Catálogo de Libros",
            style = MaterialTheme.typography.headlineSmall,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (cargando) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn {
                items(libros) { libro ->
                    LibroItem(
                        libro = libro,
                        onAgregar = { viewModel.agregarAlCarrito(libro) },
                        onEliminar = { viewModel.removerDelCarrito(libro) },
                        cantidadEnCarrito = carrito.find { it.libro.id == libro.id }?.cantidad ?: 0
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cerrar sesión")
        }
    }
}

@Composable
fun LibroItem(
    libro: Libro,
    onAgregar: () -> Unit,
    onEliminar: () -> Unit,
    cantidadEnCarrito: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = libro.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text("Stock: ${libro.cantidad}", style = MaterialTheme.typography.bodySmall)

            Spacer(modifier = Modifier.height(8.dp))

            if (cantidadEnCarrito > 0) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onEliminar) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                    }
                    Text(
                        cantidadEnCarrito.toString(),
                        modifier = Modifier.padding(horizontal = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onAgregar) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar")
                    }
                }
            } else {
                Button(onClick = onAgregar) {
                    Text("Agregar al carrito")
                }
            }
        }
    }
}
