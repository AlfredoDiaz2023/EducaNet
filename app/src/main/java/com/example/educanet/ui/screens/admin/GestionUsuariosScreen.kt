package com.example.educanet.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.viewmodel.GestionUsuariosViewModel
import com.example.educanet.viewmodel.UsuarioAdmin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionUsuariosScreen(
    onBack: () -> Unit,
    viewModel: GestionUsuariosViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedUsuario by remember { mutableStateOf<UsuarioAdmin?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Profesores", "Apoderados", "Alumnos")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Agregar usuario")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "Agregar")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Tabs para filtrar por rol
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val rolFiltrado = when (selectedTab) {
                0 -> "Profesor"
                1 -> "Apoderado"
                else -> "Alumno"
            }

            val usuariosFiltrados = uiState.usuarios.filter { it.rol == rolFiltrado }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (usuariosFiltrados.isEmpty()) {
                    Text(
                        "No hay ${tabs[selectedTab].lowercase()} registrados",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.bodyLarge
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(usuariosFiltrados, key = { it.id }) { usuario ->
                            UsuarioAdminCard(
                                usuario = usuario,
                                onEdit = {
                                    selectedUsuario = usuario
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedUsuario = usuario
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }

                uiState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                    ) {
                        Text(error)
                    }
                }
            }
        }
    }

    // Diálogo para agregar usuario
    if (showAddDialog) {
        UsuarioFormDialog(
            title = "Agregar Usuario",
            usuario = null,
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, correo, clave, rol ->
                viewModel.agregarUsuario(nombre, correo, clave, rol)
                showAddDialog = false
            }
        )
    }

    // Diálogo para editar usuario
    if (showEditDialog && selectedUsuario != null) {
        UsuarioFormDialog(
            title = "Editar Usuario",
            usuario = selectedUsuario,
            onDismiss = { showEditDialog = false },
            onConfirm = { nombre, correo, clave, rol ->
                selectedUsuario?.let { viewModel.editarUsuario(it.id, nombre, correo, clave, rol) }
                showEditDialog = false
            }
        )
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog && selectedUsuario != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar Usuario") },
            text = { Text("¿Estás seguro de que deseas eliminar a '${selectedUsuario?.nombre}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        selectedUsuario?.let { viewModel.eliminarUsuario(it.id, it.uid) }
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
fun UsuarioAdminCard(
    usuario: UsuarioAdmin,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val colorRol = when (usuario.rol) {
        "Profesor" -> Color(0xFF2196F3)
        "Apoderado" -> Color(0xFFFF9800)
        else -> Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = colorRol
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, fontWeight = FontWeight.Bold)
                Text(usuario.correo, style = MaterialTheme.typography.bodySmall)
                Surface(
                    color = colorRol.copy(alpha = 0.2f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        usuario.rol,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = colorRol
                    )
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsuarioFormDialog(
    title: String,
    usuario: UsuarioAdmin?,
    onDismiss: () -> Unit,
    onConfirm: (nombre: String, correo: String, clave: String, rol: String) -> Unit
) {
    var nombre by remember { mutableStateOf(usuario?.nombre ?: "") }
    var correo by remember { mutableStateOf(usuario?.correo ?: "") }
    var clave by remember { mutableStateOf(usuario?.clave ?: "") }
    var rolExpandido by remember { mutableStateOf(false) }
    var rolSeleccionado by remember { mutableStateOf(usuario?.rol ?: "Alumno") }

    val roles = listOf("Profesor", "Apoderado", "Alumno")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = correo,
                    onValueChange = { correo = it },
                    label = { Text("Correo electrónico") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = usuario == null // No editar correo si es edición
                )
                OutlinedTextField(
                    value = clave,
                    onValueChange = { clave = it },
                    label = { Text(if (usuario == null) "Contraseña" else "Nueva contraseña (dejar vacío para mantener)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = rolExpandido,
                    onExpandedChange = { rolExpandido = !rolExpandido }
                ) {
                    OutlinedTextField(
                        value = rolSeleccionado,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rolExpandido) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = rolExpandido,
                        onDismissRequest = { rolExpandido = false }
                    ) {
                        roles.forEach { rol ->
                            DropdownMenuItem(
                                text = { Text(rol) },
                                onClick = {
                                    rolSeleccionado = rol
                                    rolExpandido = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(nombre, correo, clave, rolSeleccionado)
                },
                enabled = nombre.isNotBlank() && (usuario != null || (correo.isNotBlank() && clave.isNotBlank()))
            ) {
                Text(if (usuario == null) "Agregar" else "Guardar")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
