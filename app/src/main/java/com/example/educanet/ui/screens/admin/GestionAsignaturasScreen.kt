package com.example.educanet.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.Asignatura
import com.example.educanet.model.NivelEducativo
import com.example.educanet.viewmodel.AsignaturaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionAsignaturasScreen(
    onBack: () -> Unit,
    asignaturaViewModel: AsignaturaViewModel = viewModel()
) {
    val uiState by asignaturaViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            asignaturaViewModel.limpiarMensaje()
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
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Gestión de Asignaturas", fontWeight = FontWeight.Bold)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF4CAF50),
                        titleContentColor = Color.White
                    )
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { asignaturaViewModel.mostrarDialogoCrear() },
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nueva Asignatura")
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Estadísticas
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EstadisticaAsignatura(
                            valor = uiState.asignaturas.size.toString(),
                            label = "Total",
                            color = Color(0xFF1565C0)
                        )
                        EstadisticaAsignatura(
                            valor = uiState.asignaturas.count { it.esObligatoria }.toString(),
                            label = "Obligatorias",
                            color = Color(0xFF4CAF50)
                        )
                        EstadisticaAsignatura(
                            valor = uiState.asignaturas.count { !it.esObligatoria }.toString(),
                            label = "Electivas",
                            color = Color(0xFFFF9800)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de asignaturas
                if (uiState.cargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                } else if (uiState.asignaturas.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay asignaturas registradas",
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Pulsa el botón + para crear una",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.asignaturas, key = { it.id }) { asignatura ->
                            AsignaturaCard(
                                asignatura = asignatura,
                                onEditar = { asignaturaViewModel.mostrarDialogoEditar(asignatura) },
                                onEliminar = { asignaturaViewModel.mostrarDialogoEliminar(asignatura) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // Diálogo crear asignatura
        if (uiState.mostrarDialogoCrear) {
            AsignaturaFormDialog(
                titulo = "Nueva Asignatura",
                nombre = uiState.nombre,
                descripcion = uiState.descripcion,
                nivelEducativo = uiState.nivelEducativo,
                cursosSeleccionados = uiState.cursosSeleccionados,
                cursosDisponibles = asignaturaViewModel.cursosDisponibles,
                esObligatoria = uiState.esObligatoria,
                guardando = uiState.guardando,
                onNombreChange = { asignaturaViewModel.actualizarNombre(it) },
                onDescripcionChange = { asignaturaViewModel.actualizarDescripcion(it) },
                onNivelChange = { asignaturaViewModel.actualizarNivelEducativo(it) },
                onCursoToggle = { asignaturaViewModel.toggleCurso(it) },
                onSeleccionarTodos = { asignaturaViewModel.seleccionarTodosLosCursos() },
                onDeseleccionarTodos = { asignaturaViewModel.deseleccionarTodosLosCursos() },
                onObligatoriaChange = { asignaturaViewModel.actualizarEsObligatoria(it) },
                onConfirmar = { asignaturaViewModel.crearAsignatura() },
                onDismiss = { asignaturaViewModel.ocultarDialogoCrear() }
            )
        }

        // Diálogo editar asignatura
        if (uiState.mostrarDialogoEditar) {
            AsignaturaFormDialog(
                titulo = "Editar Asignatura",
                nombre = uiState.nombre,
                descripcion = uiState.descripcion,
                nivelEducativo = uiState.nivelEducativo,
                cursosSeleccionados = uiState.cursosSeleccionados,
                cursosDisponibles = asignaturaViewModel.cursosDisponibles,
                esObligatoria = uiState.esObligatoria,
                guardando = uiState.guardando,
                onNombreChange = { asignaturaViewModel.actualizarNombre(it) },
                onDescripcionChange = { asignaturaViewModel.actualizarDescripcion(it) },
                onNivelChange = { asignaturaViewModel.actualizarNivelEducativo(it) },
                onCursoToggle = { asignaturaViewModel.toggleCurso(it) },
                onSeleccionarTodos = { asignaturaViewModel.seleccionarTodosLosCursos() },
                onDeseleccionarTodos = { asignaturaViewModel.deseleccionarTodosLosCursos() },
                onObligatoriaChange = { asignaturaViewModel.actualizarEsObligatoria(it) },
                onConfirmar = { asignaturaViewModel.actualizarAsignatura() },
                onDismiss = { asignaturaViewModel.ocultarDialogoEditar() }
            )
        }

        // Diálogo confirmar eliminación
        if (uiState.mostrarDialogoEliminar && uiState.asignaturaSeleccionada != null) {
            AlertDialog(
                onDismissRequest = { asignaturaViewModel.ocultarDialogoEliminar() },
                icon = {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = Color(0xFFF44336),
                        modifier = Modifier.size(48.dp)
                    )
                },
                title = {
                    Text(
                        "Eliminar Asignatura",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text("¿Estás seguro de que deseas eliminar la asignatura \"${uiState.asignaturaSeleccionada?.nombre}\"?\n\nEsta acción no se puede deshacer.")
                },
                confirmButton = {
                    Button(
                        onClick = { asignaturaViewModel.eliminarAsignatura() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                        enabled = !uiState.guardando
                    ) {
                        if (uiState.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Eliminar")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { asignaturaViewModel.ocultarDialogoEliminar() }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
private fun EstadisticaAsignatura(
    valor: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            valor,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = color
        )
        Text(
            label,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}

@Composable
private fun AsignaturaCard(
    asignatura: Asignatura,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(
                                Icons.Default.Book,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            asignatura.nombre,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (asignatura.esObligatoria) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFF4CAF50).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Obligatoria",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = Color(0xFF4CAF50),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFFF9800).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        "Electiva",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        fontSize = 10.sp,
                                        color = Color(0xFFFF9800),
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                asignatura.nivelEducativo.displayName,
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
                
                Row {
                    IconButton(onClick = onEditar) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color(0xFF2196F3)
                        )
                    }
                    IconButton(onClick = onEliminar) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color(0xFFF44336)
                        )
                    }
                }
            }

            if (asignatura.descripcion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    asignatura.descripcion,
                    fontSize = 13.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            // Cursos asociados
            Text(
                "Cursos:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(asignatura.cursos.take(6)) { curso ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1565C0).copy(alpha = 0.1f)
                    ) {
                        Text(
                            curso,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            fontSize = 11.sp,
                            color = Color(0xFF1565C0)
                        )
                    }
                }
                if (asignatura.cursos.size > 6) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Gray.copy(alpha = 0.1f)
                        ) {
                            Text(
                                "+${asignatura.cursos.size - 6}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AsignaturaFormDialog(
    titulo: String,
    nombre: String,
    descripcion: String,
    nivelEducativo: NivelEducativo,
    cursosSeleccionados: List<String>,
    cursosDisponibles: List<String>,
    esObligatoria: Boolean,
    guardando: Boolean,
    onNombreChange: (String) -> Unit,
    onDescripcionChange: (String) -> Unit,
    onNivelChange: (NivelEducativo) -> Unit,
    onCursoToggle: (String) -> Unit,
    onSeleccionarTodos: () -> Unit,
    onDeseleccionarTodos: () -> Unit,
    onObligatoriaChange: (Boolean) -> Unit,
    onConfirmar: () -> Unit,
    onDismiss: () -> Unit
) {
    var nivelExpandido by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        titulo,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Contenido scrolleable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = onNombreChange,
                        label = { Text("Nombre de la asignatura *") },
                        placeholder = { Text("Ej: Matemática") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Descripción
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = onDescripcionChange,
                        label = { Text("Descripción") },
                        placeholder = { Text("Descripción de la asignatura...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        maxLines = 5
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Nivel Educativo
                    ExposedDropdownMenuBox(
                        expanded = nivelExpandido,
                        onExpandedChange = { nivelExpandido = it }
                    ) {
                        OutlinedTextField(
                            value = nivelEducativo.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Nivel Educativo") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = nivelExpandido)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = nivelExpandido,
                            onDismissRequest = { nivelExpandido = false }
                        ) {
                            NivelEducativo.entries.forEach { nivel ->
                                DropdownMenuItem(
                                    text = { Text(nivel.displayName) },
                                    onClick = {
                                        onNivelChange(nivel)
                                        nivelExpandido = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Es Obligatoria
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Asignatura Obligatoria",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                if (esObligatoria) "Esta asignatura es obligatoria" else "Esta asignatura es electiva",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Switch(
                            checked = esObligatoria,
                            onCheckedChange = onObligatoriaChange,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cursos
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Cursos donde se imparte *",
                                    fontWeight = FontWeight.Medium
                                )
                                Row {
                                    TextButton(onClick = onSeleccionarTodos) {
                                        Text("Todos", fontSize = 12.sp)
                                    }
                                    TextButton(onClick = onDeseleccionarTodos) {
                                        Text("Ninguno", fontSize = 12.sp)
                                    }
                                }
                            }

                            Text(
                                "${cursosSeleccionados.size} cursos seleccionados",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid de cursos
                            Column {
                                // Educación Básica
                                Text(
                                    "Educación Básica",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4CAF50)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRowCursos(
                                    cursos = cursosDisponibles.filter { it.contains("Básico") },
                                    seleccionados = cursosSeleccionados,
                                    onToggle = onCursoToggle
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Educación Media
                                Text(
                                    "Educación Media",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF2196F3)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                FlowRowCursos(
                                    cursos = cursosDisponibles.filter { it.contains("Medio") },
                                    seleccionados = cursosSeleccionados,
                                    onToggle = onCursoToggle
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = onConfirmar,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        enabled = !guardando && nombre.isNotBlank() && cursosSeleccionados.isNotEmpty()
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlowRowCursos(
    cursos: List<String>,
    seleccionados: List<String>,
    onToggle: (String) -> Unit
) {
    Column {
        cursos.chunked(4).forEach { fila ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fila.forEach { curso ->
                    val isSelected = seleccionados.contains(curso)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggle(curso) },
                        label = { 
                            Text(
                                curso.replace("° Básico", "°B").replace("° Medio", "°M"),
                                fontSize = 11.sp
                            ) 
                        },
                        modifier = Modifier.weight(1f),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF4CAF50),
                            selectedLabelColor = Color.White
                        )
                    )
                }
                // Rellenar espacios vacíos para mantener el grid uniforme
                repeat(4 - fila.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}
