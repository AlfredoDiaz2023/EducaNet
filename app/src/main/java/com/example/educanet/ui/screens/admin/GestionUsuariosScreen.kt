package com.example.educanet.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.model.SolicitudVinculacion
import com.example.educanet.viewmodel.GestionUsuariosViewModel
import com.example.educanet.viewmodel.UsuarioAdmin
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

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
    var showVincularDialog by remember { mutableStateOf(false) }
    var showAsignarCursosDialog by remember { mutableStateOf(false) }
    var selectedUsuario by remember { mutableStateOf<UsuarioAdmin?>(null) }
    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("Profesores", "Apoderados", "Alumnos")
    val solicitudesPendientes = uiState.solicitudesPendientes.size

    // Limpiar mensajes automáticamente
    LaunchedEffect(uiState.successMessage) {
        if (uiState.successMessage != null) {
            delay(3000)
            viewModel.limpiarMensajes()
        }
    }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            delay(3000)
            viewModel.limpiarMensajes()
        }
    }

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
                    // Botón de solicitudes pendientes
                    if (solicitudesPendientes > 0) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                ) {
                                    Text(solicitudesPendientes.toString())
                                }
                            }
                        ) {
                            IconButton(onClick = { viewModel.toggleShowSolicitudes() }) {
                                Icon(
                                    Icons.Default.Notifications,
                                    contentDescription = "Solicitudes pendientes",
                                    tint = if (uiState.showSolicitudes) MaterialTheme.colorScheme.primary else Color.Gray
                                )
                            }
                        }
                    }
                    // Botón de asignar cursos masivamente (solo visible en tab Alumnos)
                    if (selectedTab == 2) {
                        IconButton(onClick = { showAsignarCursosDialog = true }) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = "Asignar cursos",
                                tint = Color(0xFF9C27B0)
                            )
                        }
                    }
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.PersonAdd, contentDescription = "Agregar usuario")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Panel de solicitudes pendientes (expandible)
            if (uiState.showSolicitudes && uiState.solicitudesPendientes.isNotEmpty()) {
                SolicitudesPendientesPanel(
                    solicitudes = uiState.solicitudesPendientes,
                    onAprobar = { viewModel.aprobarSolicitud(it) },
                    onRechazar = { viewModel.rechazarSolicitud(it) }
                )
            }

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
                                alumnos = if (usuario.rol == "Apoderado") uiState.alumnosDisponibles else emptyList(),
                                onEdit = {
                                    selectedUsuario = usuario
                                    showEditDialog = true
                                },
                                onDelete = {
                                    selectedUsuario = usuario
                                    showDeleteDialog = true
                                },
                                onVincular = {
                                    selectedUsuario = usuario
                                    showVincularDialog = true
                                },
                                onDesvincular = {
                                    viewModel.desvincularAlumno(usuario.id)
                                },
                                onAsignarCurso = { curso ->
                                    viewModel.asignarCurso(usuario.id, curso)
                                }
                            )
                        }
                    }
                }

                // Snackbar de éxito
                uiState.successMessage?.let { message ->
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        containerColor = Color(0xFF4CAF50)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(message, color = Color.White)
                        }
                    }
                }

                // Snackbar de error
                uiState.error?.let { error ->
                    Snackbar(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp),
                        containerColor = Color(0xFFF44336)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(error, color = Color.White)
                        }
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

    // Diálogo para vincular alumno
    if (showVincularDialog && selectedUsuario != null) {
        VincularAlumnoDialog(
            apoderado = selectedUsuario!!,
            alumnos = uiState.alumnosDisponibles,
            onDismiss = { showVincularDialog = false },
            onVincular = { alumno ->
                viewModel.vincularAlumnoManual(
                    apoderadoId = selectedUsuario!!.id,
                    alumnoId = alumno.id,
                    alumnoNombre = alumno.nombre,
                    alumnoCorreo = alumno.correo
                )
                showVincularDialog = false
            }
        )
    }

    // Diálogo para asignar cursos masivamente
    if (showAsignarCursosDialog) {
        AsignarCursosMasivosDialog(
            alumnos = uiState.alumnos,
            onDismiss = { showAsignarCursosDialog = false },
            onAsignar = { asignaciones ->
                viewModel.asignarCursosMasivos(asignaciones)
                showAsignarCursosDialog = false
            }
        )
    }
}

@Composable
fun SolicitudesPendientesPanel(
    solicitudes: List<SolicitudVinculacion>,
    onAprobar: (SolicitudVinculacion) -> Unit,
    onRechazar: (SolicitudVinculacion) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1)),
        border = BorderStroke(1.dp, Color(0xFFFFB300))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Notifications,
                        contentDescription = null,
                        tint = Color(0xFFFF8F00)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Solicitudes de Vinculación",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE65100)
                    )
                }
                Badge(
                    containerColor = Color(0xFFFF8F00)
                ) {
                    Text(solicitudes.size.toString())
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            solicitudes.forEach { solicitud ->
                SolicitudVinculacionItem(
                    solicitud = solicitud,
                    onAprobar = { onAprobar(solicitud) },
                    onRechazar = { onRechazar(solicitud) }
                )
                if (solicitud != solicitudes.last()) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun SolicitudVinculacionItem(
    solicitud: SolicitudVinculacion,
    onAprobar: () -> Unit,
    onRechazar: () -> Unit
) {
    val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val fechaFormateada = dateFormatter.format(Date(solicitud.fechaSolicitud))

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Apoderado: ${solicitud.apoderadoNombre}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        solicitud.apoderadoCorreo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color(0xFFFF8F00),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Alumno: ${solicitud.alumnoNombre}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        solicitud.alumnoCorreo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Solicitado: $fechaFormateada",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Row {
                    OutlinedButton(
                        onClick = onRechazar,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        border = BorderStroke(1.dp, Color.Red),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onAprobar,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
fun UsuarioAdminCard(
    usuario: UsuarioAdmin,
    alumnos: List<UsuarioAdmin> = emptyList(),
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onVincular: () -> Unit = {},
    onDesvincular: () -> Unit = {},
    onAsignarCurso: (String) -> Unit = {} // Nuevo parámetro
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
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colorRol.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(28.dp),
                        tint = colorRol
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(usuario.nombre, fontWeight = FontWeight.Bold)
                    Text(usuario.correo, style = MaterialTheme.typography.bodySmall)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
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
                        // Mostrar curso para alumnos
                        if (usuario.rol == "Alumno" && usuario.curso.isNotEmpty()) {
                            Surface(
                                color = Color(0xFF9C27B0).copy(alpha = 0.2f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    usuario.curso,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color(0xFF9C27B0)
                                )
                            }
                        }
                    }
                }

                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color(0xFF2196F3))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
            }

            // Mostrar info de alumno vinculado para apoderados
            if (usuario.rol == "Apoderado") {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                if (usuario.alumnoVinculadoId.isNotEmpty()) {
                    // Tiene alumno vinculado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Vinculado a:",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.Gray
                                )
                                Text(
                                    usuario.alumnoVinculadoNombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = onDesvincular,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = BorderStroke(1.dp, Color.Red),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.LinkOff,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Desvincular", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                } else {
                    // Sin alumno vinculado
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LinkOff,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sin alumno vinculado",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFFF9800)
                            )
                        }
                        Button(
                            onClick = onVincular,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Link,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Vincular", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Sección para asignar curso a alumnos
            if (usuario.rol == "Alumno") {
                var cursoExpandido by remember { mutableStateOf(false) }
                val cursos = listOf(
                    "1° Básico", "2° Básico", "3° Básico", "4° Básico",
                    "5° Básico", "6° Básico", "7° Básico", "8° Básico",
                    "1° Medio", "2° Medio", "3° Medio", "4° Medio"
                )

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Curso asignado:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray
                            )
                            Text(
                                if (usuario.curso.isEmpty()) "Sin asignar" else usuario.curso,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (usuario.curso.isEmpty()) Color(0xFFFF9800) else Color(0xFF9C27B0)
                            )
                        }
                    }

                    Box {
                        OutlinedButton(
                            onClick = { cursoExpandido = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF9C27B0)),
                            border = BorderStroke(1.dp, Color(0xFF9C27B0)),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                if (usuario.curso.isEmpty()) "Asignar" else "Cambiar",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        DropdownMenu(
                            expanded = cursoExpandido,
                            onDismissRequest = { cursoExpandido = false }
                        ) {
                            cursos.forEach { curso ->
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text(curso)
                                            if (curso == usuario.curso) {
                                                Icon(
                                                    Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color(0xFF4CAF50),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        onAsignarCurso(curso)
                                        cursoExpandido = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun VincularAlumnoDialog(
    apoderado: UsuarioAdmin,
    alumnos: List<UsuarioAdmin>,
    onDismiss: () -> Unit,
    onVincular: (UsuarioAdmin) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Título
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Vincular Alumno",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "Selecciona el alumno para vincular a ${apoderado.nombre}:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (alumnos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No hay alumnos disponibles",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        alumnos.forEach { alumno ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onVincular(alumno) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            alumno.nombre.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF4CAF50)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            alumno.nombre,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            alumno.correo,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                    Icon(
                                        Icons.Default.ChevronRight,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignarCursosMasivosDialog(
    alumnos: List<UsuarioAdmin>,
    onDismiss: () -> Unit,
    onAsignar: (Map<String, String>) -> Unit
) {
    val cursos = listOf(
        "1° Básico", "2° Básico", "3° Básico", "4° Básico",
        "5° Básico", "6° Básico", "7° Básico", "8° Básico",
        "1° Medio", "2° Medio", "3° Medio", "4° Medio"
    )
    
    // Estado para las asignaciones
    val asignaciones = remember { mutableStateMapOf<String, String>() }
    var cursoSeleccionado by remember { mutableStateOf("") }
    var cursoExpandido by remember { mutableStateOf(false) }
    
    // Inicializar con los cursos actuales
    LaunchedEffect(alumnos) {
        alumnos.forEach { alumno ->
            if (alumno.curso.isNotEmpty()) {
                asignaciones[alumno.id] = alumno.curso
            }
        }
    }
    
    // Alumnos sin curso asignado
    val alumnosSinCurso = alumnos.filter { it.curso.isEmpty() }
    val alumnosConCurso = alumnos.filter { it.curso.isNotEmpty() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
                .padding(8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Título
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF9C27B0),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Asignar Cursos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Resumen
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = Color(0xFFFF9800).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "⚠️ ${alumnosSinCurso.size} sin curso",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100)
                        )
                    }
                    Surface(
                        color = Color(0xFF4CAF50).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "✓ ${alumnosConCurso.size} con curso",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Asignación rápida (para alumnos sin curso)
                if (alumnosSinCurso.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "Asignación Rápida",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ExposedDropdownMenuBox(
                                    expanded = cursoExpandido,
                                    onExpandedChange = { cursoExpandido = !cursoExpandido },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = cursoSeleccionado.ifEmpty { "Seleccionar curso" },
                                        onValueChange = {},
                                        readOnly = true,
                                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cursoExpandido) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                        textStyle = MaterialTheme.typography.bodySmall
                                    )
                                    ExposedDropdownMenu(
                                        expanded = cursoExpandido,
                                        onDismissRequest = { cursoExpandido = false }
                                    ) {
                                        cursos.forEach { curso ->
                                            DropdownMenuItem(
                                                text = { Text(curso) },
                                                onClick = {
                                                    cursoSeleccionado = curso
                                                    cursoExpandido = false
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (cursoSeleccionado.isNotEmpty()) {
                                            alumnosSinCurso.forEach { alumno ->
                                                asignaciones[alumno.id] = cursoSeleccionado
                                            }
                                        }
                                    },
                                    enabled = cursoSeleccionado.isNotEmpty(),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                                ) {
                                    Text("Asignar a todos", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Lista de alumnos
                Text(
                    "Alumnos (${alumnos.size})",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(alumnos, key = { it.id }) { alumno ->
                        var alumnoExpanded by remember { mutableStateOf(false) }
                        val cursoActual = asignaciones[alumno.id] ?: alumno.curso

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (cursoActual.isEmpty()) Color(0xFFFFF8E1) else Color(0xFFF5F5F5)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50).copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        alumno.nombre.take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4CAF50),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        alumno.nombre,
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        alumno.correo,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }

                                Box {
                                    OutlinedButton(
                                        onClick = { alumnoExpanded = true },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (cursoActual.isEmpty()) Color(0xFFFF9800) else Color(0xFF9C27B0)
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (cursoActual.isEmpty()) Color(0xFFFF9800) else Color(0xFF9C27B0)
                                        )
                                    ) {
                                        Text(
                                            cursoActual.ifEmpty { "Asignar" },
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = alumnoExpanded,
                                        onDismissRequest = { alumnoExpanded = false }
                                    ) {
                                        cursos.forEach { curso ->
                                            DropdownMenuItem(
                                                text = {
                                                    Row(
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        modifier = Modifier.fillMaxWidth()
                                                    ) {
                                                        Text(curso)
                                                        if (curso == cursoActual) {
                                                            Icon(
                                                                Icons.Default.Check,
                                                                contentDescription = null,
                                                                tint = Color(0xFF4CAF50),
                                                                modifier = Modifier.size(16.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    asignaciones[alumno.id] = curso
                                                    alumnoExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = { 
                            onAsignar(asignaciones.filter { it.value.isNotEmpty() })
                        },
                        modifier = Modifier.weight(1f),
                        enabled = asignaciones.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                    ) {
                        Icon(
                            Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Guardar (${asignaciones.size})")
                    }
                }
            }
        }
    }
}
