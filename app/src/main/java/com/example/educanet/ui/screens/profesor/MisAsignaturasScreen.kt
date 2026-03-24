package com.example.educanet.ui.screens.profesor

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.*
import com.example.educanet.viewmodel.AsignaturaViewModel
import com.example.educanet.viewmodel.HorarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MisAsignaturasScreen(
    profesorNombre: String,
    onBack: () -> Unit,
    onIniciarClase: (asignatura: String, curso: String) -> Unit,
    asignaturaViewModel: AsignaturaViewModel = viewModel(),
    horarioViewModel: HorarioViewModel = viewModel()
) {
    val asignaturaState by asignaturaViewModel.uiState.collectAsState()
    val horarioState by horarioViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Estado para selección
    var asignaturaSeleccionada by remember { mutableStateOf<Asignatura?>(null) }
    var cursoSeleccionado by remember { mutableStateOf<String?>(null) }
    var mostrarDialogoIniciarClase by remember { mutableStateOf(false) }
    var filtroNivel by remember { mutableStateOf("Todos") }
    
    val niveles = listOf("Todos", "Básica", "Media")
    
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
                            Text("Mis Asignaturas", fontWeight = FontWeight.Bold)
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
                        containerColor = Color(0xFF3F51B5),
                        titleContentColor = Color.White
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Saludo al profesor
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3F51B5).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF3F51B5),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Profesor: $profesorNombre",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color(0xFF3F51B5)
                            )
                            Text(
                                "Seleccione una asignatura y curso para enseñar",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF3F51B5).copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Filtros por nivel
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(niveles) { nivel ->
                        FilterChip(
                            selected = filtroNivel == nivel,
                            onClick = { filtroNivel = nivel },
                            label = { Text(nivel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3F51B5),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Lista de asignaturas
                if (asignaturaState.cargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF3F51B5))
                    }
                } else {
                    val asignaturasFiltradas = asignaturaState.asignaturas.filter { asignatura ->
                        when (filtroNivel) {
                            "Básica" -> asignatura.nivelEducativo == NivelEducativo.EDUCACION_BASICA || 
                                       asignatura.nivelEducativo == NivelEducativo.TODOS
                            "Media" -> asignatura.nivelEducativo == NivelEducativo.EDUCACION_MEDIA || 
                                      asignatura.nivelEducativo == NivelEducativo.EDUCACION_MEDIA_SUPERIOR ||
                                      asignatura.nivelEducativo == NivelEducativo.TODOS
                            else -> true
                        }
                    }
                    
                    if (asignaturasFiltradas.isEmpty()) {
                        EmptyAsignaturasState()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(asignaturasFiltradas) { asignatura ->
                                AsignaturaProfesorCard(
                                    asignatura = asignatura,
                                    onClick = {
                                        asignaturaSeleccionada = asignatura
                                        mostrarDialogoIniciarClase = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Diálogo para seleccionar curso e iniciar clase
        if (mostrarDialogoIniciarClase && asignaturaSeleccionada != null) {
            SeleccionarCursoDialog(
                asignatura = asignaturaSeleccionada!!,
                horarioState = horarioState,
                onCursoSeleccionado = { curso ->
                    cursoSeleccionado = curso
                },
                onIniciarClase = {
                    cursoSeleccionado?.let { curso ->
                        onIniciarClase(asignaturaSeleccionada!!.nombre, curso)
                    }
                    mostrarDialogoIniciarClase = false
                    asignaturaSeleccionada = null
                    cursoSeleccionado = null
                },
                onDismiss = {
                    mostrarDialogoIniciarClase = false
                    asignaturaSeleccionada = null
                    cursoSeleccionado = null
                }
            )
        }
    }
}

@Composable
fun AsignaturaProfesorCard(
    asignatura: Asignatura,
    onClick: () -> Unit
) {
    val colorAsignatura = when (asignatura.nombre) {
        "Lenguaje y Comunicación", "Lengua y Literatura" -> Color(0xFFE53935)
        "Matemática", "Matemática Avanzada" -> Color(0xFF1E88E5)
        "Historia, Geografía y Ciencias Sociales" -> Color(0xFF8D6E63)
        "Ciencias Naturales", "Ciencias para la Ciudadanía" -> Color(0xFF43A047)
        "Inglés" -> Color(0xFF5E35B1)
        "Educación Física y Salud" -> Color(0xFFFF8F00)
        "Artes Visuales", "Música" -> Color(0xFFD81B60)
        "Tecnología" -> Color(0xFF00ACC1)
        "Filosofía" -> Color(0xFF6D4C41)
        "Educación Ciudadana" -> Color(0xFF7CB342)
        else -> Color(0xFF3F51B5)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icono de la asignatura
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = colorAsignatura.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = null,
                        tint = colorAsignatura,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asignatura.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = asignatura.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    AssistChip(
                        onClick = {},
                        label = { 
                            Text(
                                "${asignatura.cursos.size} cursos",
                                fontSize = 10.sp
                            ) 
                        },
                        modifier = Modifier.height(24.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = colorAsignatura.copy(alpha = 0.1f)
                        )
                    )
                }
            }
            
            Icon(
                imageVector = Icons.Default.PlayCircle,
                contentDescription = "Enseñar",
                tint = colorAsignatura,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarCursoDialog(
    asignatura: Asignatura,
    horarioState: com.example.educanet.viewmodel.HorarioUiState,
    onCursoSeleccionado: (String) -> Unit,
    onIniciarClase: () -> Unit,
    onDismiss: () -> Unit
) {
    var cursoSeleccionado by remember { mutableStateOf<String?>(null) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = null,
                            tint = Color(0xFF3F51B5),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Iniciar Clase",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                asignatura.nombre,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF3F51B5)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    "Selecciona el curso:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Lista de cursos disponibles para esta asignatura
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(asignatura.cursos) { curso ->
                        val horarioCurso = horarioState.horarios.find { it.curso == curso }
                        val isSelected = cursoSeleccionado == curso
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    cursoSeleccionado = curso
                                    onCursoSeleccionado(curso)
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) 
                                    Color(0xFF3F51B5).copy(alpha = 0.15f) 
                                else 
                                    Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = if (isSelected) 
                                androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF3F51B5)) 
                            else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        curso,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    if (horarioCurso != null) {
                                        // Obtener el día actual
                                        val diaActual = obtenerDiaActual()
                                        val horarioDia = horarioCurso.horarioSemanal[diaActual]
                                        Text(
                                            "Hoy: ${horarioDia?.horaEntrada ?: "08:00"} - ${horarioDia?.horaSalida ?: "15:30"}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                
                                if (isSelected) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Seleccionado",
                                        tint = Color(0xFF3F51B5),
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF3F51B5)
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = onIniciarClase,
                        modifier = Modifier.weight(1f),
                        enabled = cursoSeleccionado != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Iniciar Clase")
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyAsignaturasState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No hay asignaturas disponibles",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                "Las asignaturas se cargarán automáticamente",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Obtiene el día de la semana actual
 */
private fun obtenerDiaActual(): DiaSemana {
    val calendar = java.util.Calendar.getInstance()
    return when (calendar.get(java.util.Calendar.DAY_OF_WEEK)) {
        java.util.Calendar.MONDAY -> DiaSemana.LUNES
        java.util.Calendar.TUESDAY -> DiaSemana.MARTES
        java.util.Calendar.WEDNESDAY -> DiaSemana.MIERCOLES
        java.util.Calendar.THURSDAY -> DiaSemana.JUEVES
        java.util.Calendar.FRIDAY -> DiaSemana.VIERNES
        else -> DiaSemana.LUNES // Fin de semana, mostrar lunes por defecto
    }
}
