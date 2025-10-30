package com.example.educanet.ui.screens.libro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.repository.LibroRepository
import com.example.educanet.viewmodel.CarritoViewModel
import com.example.educanet.model.Libro
import kotlinx.coroutines.launch
import coil.compose.AsyncImage

@Composable
fun LibroScreen(
    onBack: () -> Unit,
    viewModel: CarritoViewModel = viewModel()
) {
    val libroRepository = remember { LibroRepository() }
    val scope = rememberCoroutineScope()

    var libros by remember { mutableStateOf<List<Libro>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val carrito by viewModel.carrito.collectAsState()

    LaunchedEffect(Unit) {
        scope.launch {
            val resultado = libroRepository.obtenerLibros()
            libros = resultado.libros
            cargando = false
        }
    }

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
            Text(
                "Libros y Artículos",
                style = MaterialTheme.typography.headlineSmall
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ✅ Mostrar imagen si está disponible
            if (libro.imagen.isNotEmpty()) {
                AsyncImage(
                    model = libro.imagen,
                    contentDescription = libro.nombre,
                    modifier = Modifier
                        .size(140.dp)
                        .padding(bottom = 8.dp)
                )
            }

            Text(libro.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Nivel: ${libro.nivel}")
            Text("Stock: ${libro.cantidad}")

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
