package com.example.educanet.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.educanet.model.Libro
import com.example.educanet.viewmodel.GestionLibrosViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionLibrosScreen(
    onBack: () -> Unit,
    viewModel: GestionLibrosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedLibro by remember { mutableStateOf<Libro?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Libros") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar libro")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.libros.isEmpty()) {
                Text(
                    "No hay libros registrados",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.libros, key = { it.id }) { libro ->
                        LibroAdminCard(
                            libro = libro,
                            onEdit = {
                                selectedLibro = libro
                                showEditDialog = true
                            },
                            onDelete = {
                                selectedLibro = libro
                                showDeleteDialog = true
                            }
                        )
                    }
                }
            }

            // Mensaje de error
            uiState.error?.let { error ->
                Snackbar(
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                ) {
                    Text(error)
                }
            }
        }
    }

    // Diálogo para agregar libro
    if (showAddDialog) {
        LibroFormDialog(
            title = "Agregar Libro",
            libro = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, nivel, imagen, cantidad ->
                viewModel.agregarLibro(nombre, nivel, imagen, cantidad)
                showAddDialog = false
            }
        )
    }

    // Diálogo para editar libro
    if (showEditDialog && selectedLibro != null) {
        LibroFormDialog(
            title = "Editar Libro",
            libro = selectedLibro,
            onDismiss = { showEditDialog = false },
            onConfirm = { nombre, nivel, imagen, cantidad ->
                selectedLibro?.let { viewModel.editarLibro(it.id, nombre, nivel, imagen, cantidad) }
                showEditDialog = false
            }
        )
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && selectedLibro != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Libro") },
            text = { Text("¿Estás seguro de que deseas eliminar '${selectedLibro?.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedLibro?.let { viewModel.eliminarLibro(it.id) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun LibroAdminCard(
    libro: Libro,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (libro.imagen.isNotEmpty()) {
                AsyncImage(
                    model = libro.imagen,
                    contentDescription = libro.nombre,
                    modifier = Modifier.size(60.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(libro.nombre, fontWeight = FontWeight.Bold)
                Text("Nivel: ${libro.nivel}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Stock: ${libro.cantidad}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (libro.cantidad > 0) Color(0xFF4CAF50) else Color.Red
                )
            }

            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF2196F3))
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
            }
        }
    }
}

@Composable
fun LibroFormDialog(
    title: String,
    libro: Libro?,
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, nivel: String, imagen: String, cantidad: Int) -> Unit
) {
    var nombre by remember { mutableStateOf(libro?.nombre ?: "") }
    var nivel by remember { mutableStateOf(libro?.nivel ?: "") }
    var imagen by remember { mutableStateOf(libro?.imagen ?: "") }
    var cantidad by remember { mutableStateOf(libro?.cantidad?.toString() ?: "0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del libro") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = nivel,
                    onValueChange = { nivel = it },
                    label = { Text("Nivel") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = imagen,
                    onValueChange = { imagen = it },
                    label = { Text("URL de la imagen") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = cantidad,
                    onValueChange = { cantidad = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad en stock") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(nombre, nivel, imagen, cantidad.toIntOrNull() ?: 0)
                },
                enabled = nombre.isNotBlank()
            ) {
                Text(if (libro == null) "Agregar" else "Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
