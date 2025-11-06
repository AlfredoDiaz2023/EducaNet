package com.example.educanet.ui.screens.libro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.educanet.model.Libro
import com.example.educanet.repository.LibroRepository
import com.example.educanet.repository.NotificacionRepository
import kotlinx.coroutines.launch

@Composable
fun LibroScreen(
    rol: String,
    nombre: String,
    onBack: () -> Unit,
    onAddLibro: () -> Unit
) {
    val libroRepository = remember { LibroRepository() }
    val notificacionRepository = remember { NotificacionRepository() }
    val scope = rememberCoroutineScope()

    var libros by remember { mutableStateOf<List<Libro>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    fun cargarLibros() {
        scope.launch {
            cargando = true
            val resultado = libroRepository.obtenerLibros()
            libros = resultado.libros
            cargando = false
        }
    }

    LaunchedEffect(Unit) {
        cargarLibros()
    }

    suspend fun handleSolicitarLibro(libro: Libro) {
        if (libro.cantidad > 0) {
            val nuevoStock = libro.cantidad - 1
            val success = libroRepository.actualizarStock(libro.id, nuevoStock)

            if (success) {
                notificacionRepository.agregarNotificacion(
                    titulo = "Solicitud de libro",
                    mensaje = "El usuario $nombre ($rol) ha solicitado el libro: ${libro.nombre}"
                )
                // Actualizar la lista localmente
                val updatedLibros = libros.map {
                    if (it.id == libro.id) {
                        it.copy(cantidad = nuevoStock)
                    } else {
                        it
                    }
                }
                libros = updatedLibros
            }
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
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(libros, key = { it.id }) { libro ->
                    LibroItem(
                        libro = libro,
                        onSolicitar = { handleSolicitarLibro(libro) }
                    )
                }
            }
        }

        if (rol == "Profesor") {
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onAddLibro,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Agregar Libro")
            }
        }
    }
}

@Composable
fun LibroItem(
    libro: Libro,
    onSolicitar: suspend () -> Unit
) {
    var isRequesting by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val estado = if (libro.cantidad > 0) "Disponible" else "No disponible"
    val colorEstado = if (libro.cantidad > 0) Color(0xFF4CAF50) else Color.Red

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
            Text("Cantidad: ${libro.cantidad}")
            Text(estado, color = colorEstado, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        isRequesting = true
                        try {
                            onSolicitar()
                        } finally {
                            isRequesting = false
                        }
                    }
                },
                enabled = libro.cantidad > 0 && !isRequesting
            ) {
                if (isRequesting) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Solicitar")
                }
            }
        }
    }
}
