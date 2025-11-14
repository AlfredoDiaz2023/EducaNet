package com.example.educanet.ui.screens.libro

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.educanet.R
import com.example.educanet.model.Libro
import com.example.educanet.model.Resena
import com.example.educanet.viewmodel.LibroViewModel
import kotlinx.coroutines.launch

@Composable
fun LibroScreen(
    rol: String,
    nombre: String,
    onBack: () -> Unit,
    onAddLibro: () -> Unit,
    viewModel: LibroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showResenasDialog by remember { mutableStateOf(false) }
    var showAddResenaDialog by remember { mutableStateOf(false) }
    var selectedLibro by remember { mutableStateOf<Libro?>(null) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.08f
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp)) // Espacio superior

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        "Libros y Artículos",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Text(
                    "Total: ${uiState.libros.size}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.libros, key = { it.id }) { libro ->
                        LibroItem(
                            libro = libro,
                            onSolicitar = { viewModel.solicitarLibro(libro, nombre, rol) },
                            onReservar = { viewModel.reservarLibro(libro, nombre) },
                            onVerResenas = {
                                selectedLibro = libro
                                viewModel.obtenerResenas(libro.id)
                                showResenasDialog = true
                            },
                            onAddResena = {
                                selectedLibro = libro
                                showAddResenaDialog = true
                            }
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

    if (showResenasDialog) {
        selectedLibro?.let {
            ResenasDialog(
                libro = it,
                resenas = uiState.resenas,
                onDismiss = { showResenasDialog = false }
            )
        }
    }

    if (showAddResenaDialog) {
        selectedLibro?.let {
            AddResenaDialog(
                libro = it,
                onDismiss = { showAddResenaDialog = false },
                onAddResena = { rating, comment ->
                    viewModel.agregarResena(it.id, rating, comment)
                    showAddResenaDialog = false
                }
            )
        }
    }
}

@Composable
fun LibroItem(
    libro: Libro,
    onSolicitar: () -> Unit,
    onReservar: () -> Unit,
    onVerResenas: () -> Unit,
    onAddResena: () -> Unit
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
            Row {
                // Botón Solicitar (solo activo si hay stock)
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
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Solicitar")
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Botón Reservar (siempre visible)
                Button(
                    onClick = onReservar,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF44336),
                        contentColor = Color.White
                    )
                ) {
                    Text("Reservar")
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Button(onClick = onVerResenas, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BCD4), contentColor = Color.White)) {
                    Text("Ver Reseñas")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onAddResena, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800), contentColor = Color.White)) {
                    Text("Dejar Reseña")
                }
            }
        }
    }
}

@Composable
fun ResenasDialog(
    libro: Libro,
    resenas: List<Resena>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reseñas de ${libro.nombre}") },
        text = {
            if (resenas.isEmpty()) {
                Text("No hay reseñas para este libro.")
            } else {
                LazyColumn {
                    items(resenas) { resena ->
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Text(resena.userName, fontWeight = FontWeight.Bold)
                            RatingBar(rating = resena.rating, isReadOnly = true)
                            Text(resena.comment)
                            Divider(modifier = Modifier.padding(top = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

@Composable
fun AddResenaDialog(
    libro: Libro,
    onDismiss: () -> Unit,
    onAddResena: (Float, String) -> Unit
) {
    var rating by remember { mutableStateOf(0f) }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar reseña para ${libro.nombre}") },
        text = {
            Column {
                Text("Tu calificación:")
                RatingBar(rating = rating, onRatingChange = { rating = it })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Comentario") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = { onAddResena(rating, comment) }) {
                Text("Agregar")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun RatingBar(
    rating: Float,
    onRatingChange: ((Float) -> Unit)? = null,
    isReadOnly: Boolean = false
) {
    Row {
        (1..5).forEach { index ->
            val isSelected = index <= rating
            Icon(
                imageVector = if (isSelected) Icons.Filled.Star else Icons.Filled.StarBorder,
                contentDescription = null, // decorative element
                tint = Color(0xFFFFC107),
                modifier = Modifier.then(
                    if (!isReadOnly && onRatingChange != null) {
                        Modifier.clickable { onRatingChange(index.toFloat()) }
                    } else {
                        Modifier
                    }
                )
            )
        }
    }
}
