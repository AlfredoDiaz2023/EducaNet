package com.example.educanet.ui.screens.libro

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.educanet.R
import com.example.educanet.model.Libro
import com.example.educanet.model.Resena
import com.example.educanet.viewmodel.CarritoViewModel
import com.example.educanet.viewmodel.LibroViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibroScreen(
    rol: String,
    nombre: String,
    carritoViewModel: CarritoViewModel,
    onBack: () -> Unit,
    onAddLibro: () -> Unit,
    onCarritoClick: () -> Unit,
    viewModel: LibroViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddResenaDialog by remember { mutableStateOf(false) }
    var selectedLibro by remember { mutableStateOf<Libro?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    // Mostrar mensaje de error
    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearMessages()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con logo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.06f
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "📚 Biblioteca",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        // Badge del carrito
                        IconButton(onClick = onCarritoClick) {
                            Icon(
                                Icons.Default.ShoppingCart,
                                contentDescription = "Carrito",
                                tint = Color(0xFFFF9800)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                if (rol == "Profesor") {
                    ExtendedFloatingActionButton(
                        onClick = onAddLibro,
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Libro", fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
            ) {
                // Estadísticas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EstadisticaLibro(
                            icon = Icons.AutoMirrored.Filled.MenuBook,
                            valor = uiState.libros.size.toString(),
                            label = "Total Libros",
                            color = Color(0xFF2196F3)
                        )
                        EstadisticaLibro(
                            icon = Icons.Default.CheckCircle,
                            valor = uiState.libros.count { it.cantidad > 0 }.toString(),
                            label = "Disponibles",
                            color = Color(0xFF4CAF50)
                        )
                        EstadisticaLibro(
                            icon = Icons.Default.Cancel,
                            valor = uiState.libros.count { it.cantidad == 0 }.toString(),
                            label = "Agotados",
                            color = Color(0xFFF44336)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (uiState.libros.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay libros disponibles",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(uiState.libros, key = { it.id }) { libro ->
                            LibroItemModerno(
                                libro = libro,
                                onReservar = { 
                                    if (libro.cantidad > 0) {
                                        carritoViewModel.addToCart(libro)
                                        viewModel.reservarLibroAlCarrito(libro)
                                    } else {
                                        viewModel.mostrarErrorSinStock()
                                    }
                                },
                                onAddResena = {
                                    selectedLibro = libro
                                    showAddResenaDialog = true
                                }
                            )
                        }
                    }
                }
            }
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
fun EstadisticaLibro(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun LibroItemModerno(
    libro: Libro,
    onReservar: () -> Unit,
    onAddResena: () -> Unit
) {
    val disponible = libro.cantidad > 0
    val colorEstado = if (disponible) Color(0xFF4CAF50) else Color(0xFFF44336)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Imagen del libro
            Card(
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.size(100.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFFE3F2FD),
                                    Color(0xFFBBDEFB)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (libro.imagen.isNotEmpty()) {
                        AsyncImage(
                            model = libro.imagen,
                            contentDescription = libro.nombre,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF1976D2)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Información del libro
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    libro.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Nivel
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFF2196F3)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        libro.nivel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Stock
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Inventory,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = colorEstado
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Stock: ${libro.cantidad}",
                        style = MaterialTheme.typography.bodySmall,
                        color = colorEstado,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Badge de estado
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colorEstado.copy(alpha = 0.15f)
                ) {
                    Text(
                        if (disponible) "✓ Disponible" else "✗ Agotado",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorEstado,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Botones
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Reservar
                    Button(
                        onClick = onReservar,
                        enabled = disponible,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3),
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reservar", style = MaterialTheme.typography.labelMedium)
                    }

                    // Botón Reseña
                    OutlinedButton(
                        onClick = onAddResena,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFFF9800)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(listOf(Color(0xFFFF9800), Color(0xFFFF9800)))
                        ),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reseña", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
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
