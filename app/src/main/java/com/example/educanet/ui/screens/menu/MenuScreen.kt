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
import androidx.compose.ui.graphics.Color
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
    onLibroClick: () -> Unit = {},
    onVideoClick: () -> Unit = {},
    onClaseVirtualClick: () -> Unit = {},
    onTutoriaClick: () -> Unit = {},
    onProgresoAcademicoClick: () -> Unit = {},
    onVerCarrito: () -> Unit,
    onLogout: () -> Unit,
    viewModel: CarritoViewModel = viewModel()
) {
    val carrito by viewModel.carrito.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Header con nombre y carrito
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Bienvenido, $nombre", fontWeight = FontWeight.Bold)
                Text("Rol: $rol", fontSize = 14.sp)
            }

            // Icono del carrito con badge
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
            "Menu EducaNet",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF2694EE),
            fontSize = 30.sp
        )

        Spacer(Modifier.height(24.dp))

        // ✅ Opción para ir a Libros y artículos
        TextButton(onClick = onLibroClick) {
            Text("Libros y Artículos", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onVideoClick) {
            Text("Videos de Apoyo", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onClaseVirtualClick) {
            Text("Clases Virtuales", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onTutoriaClick) {
            Text("Tutorías", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onProgresoAcademicoClick) {
            Text("Progreso Académico", color = Color(0xFF090909), fontSize = 24.sp)
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
