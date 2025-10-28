package com.example.educanet.ui.screens.libro

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.educanet.repository.LibroRepository
import com.example.educanet.model.Libro
import kotlinx.coroutines.launch

@Composable
fun LibroScreen(onBack: () -> Unit) {
    val libroRepository = remember { LibroRepository() }
    val scope = rememberCoroutineScope()

    var libros by remember { mutableStateOf<List<Libro>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }


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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(libro.nombre, style = MaterialTheme.typography.titleMedium)
                            Text("Nivel: ${libro.nivel}")
                            Text("Stock: ${libro.cantidad}")
                        }
                    }
                }
            }
        }
    }
}