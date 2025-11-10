package com.example.educanet.ui.screens.libro

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.educanet.model.Libro
import com.example.educanet.repository.LibroRepository
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.ui.common.Logo
import com.example.educanet.R
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
                libros = libros.map {
                    if (it.id == libro.id) it.copy(cantidad = nuevoStock) else it
                }
            }
        }
    }

    // Fondo con imagen + contenido principal
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.08f // transparencia
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo superior más pequeño y con algo de transparencia
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo EducaNet",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(16.dp))

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
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Agregar Libro")
                }
            }
        }
    }
}

// Elemento individual para cada libro en la lista
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3881EC),
                    contentColor = Color.White
                ),
                enabled = libro.cantidad > 0 && !isRequesting
            ) {
                if (isRequesting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Solicitar")
                }
            }
        }
    }
}
