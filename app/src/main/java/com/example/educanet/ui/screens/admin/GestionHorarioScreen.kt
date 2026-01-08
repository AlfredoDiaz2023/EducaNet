package com.example.educanet.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
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
import com.example.educanet.model.BloqueHorario
import com.example.educanet.model.DiaSemana
import com.example.educanet.model.HorarioConstantes
import com.example.educanet.model.HorarioEscolar
import com.example.educanet.model.HorariosPredeterminados
import com.example.educanet.model.TipoBloque
import com.example.educanet.viewmodel.HorarioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionHorarioScreen(
    onBack: () -> Unit,
    horarioViewModel: HorarioViewModel = viewModel()
) {
    val uiState by horarioViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var mostrarDialogoReinicializar by remember { mutableStateOf(false) }
    
    // Colores del gradiente
    val gradientColors = listOf(Color(0xFF00897B), Color(0xFF26A69A))
    
    // Mostrar mensaje
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            horarioViewModel.limpiarMensaje()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF00897B).copy(alpha = 0.1f),
                        Color(0xFFF5F5F5),
                        Color.White
                    )
                )
            )
    ) {
        // Círculos decorativos de fondo
        Box(
            modifier = Modifier
                .size(180.dp)
                .offset(x = (-40).dp, y = (-40).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF00897B).copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { mostrarDialogoReinicializar = true },
                    containerColor = Color(0xFF00897B),
                    contentColor = Color.White
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Reinicializar horarios con asignaturas"
                    )
                }
            },
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(gradientColors)
                        )
                ) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        "Gestión de Horarios",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        "Jornada Escolar Completa",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
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
                            containerColor = Color.Transparent,
                            titleContentColor = Color.White
                        )
                    )
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Información de Jornada Escolar Completa con gradiente
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(8.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF00897B).copy(alpha = 0.15f),
                                        Color(0xFF26A69A).copy(alpha = 0.1f)
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        Brush.linearGradient(gradientColors),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    "📚 Jornada Escolar Completa",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color(0xFF00897B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = Color(0xFF00897B).copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Entrada: 8:00 AM • Salida variable",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF00897B).copy(alpha = 0.8f)
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Título de filtros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.FilterList,
                        contentDescription = null,
                        tint = Color(0xFF00897B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Filtrar por Nivel",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D3436)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Filtros por nivel con diseño mejorado
                var filtroNivel by remember { mutableStateOf("Todos") }
                val niveles = listOf("Todos", "Básica", "Media")
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(niveles) { nivel ->
                        val nivelEmoji = when (nivel) {
                            "Todos" -> "📋"
                            "Básica" -> "📚"
                            "Media" -> "🎓"
                            else -> ""
                        }
                        FilterChip(
                            selected = filtroNivel == nivel,
                            onClick = { filtroNivel = nivel },
                            label = { 
                                Text(
                                    "$nivelEmoji $nivel",
                                    fontWeight = if (filtroNivel == nivel) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00897B),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = Color(0xFF00897B).copy(alpha = 0.3f),
                                enabled = true,
                                selected = filtroNivel == nivel
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Título de lista
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ViewList,
                        contentDescription = null,
                        tint = Color(0xFF00897B),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Horarios por Curso",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2D3436)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Lista de horarios
                if (uiState.cargando) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                color = Color(0xFF00897B),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Cargando horarios...",
                                color = Color(0xFF00897B).copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    val horariosFiltrados = uiState.horarios.filter { horario ->
                        when (filtroNivel) {
                            "Básica" -> horario.curso.contains("Básico")
                            "Media" -> horario.curso.contains("Medio")
                            else -> true
                        }
                    }
                    
                    if (horariosFiltrados.isEmpty()) {
                        EmptyHorarioState()
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(horariosFiltrados) { horario ->
                                HorarioCardAdmin(
                                    horario = horario,
                                    gradientColors = gradientColors,
                                    onClick = { horarioViewModel.mostrarDialogoEditar(horario) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Diálogo para editar horario
        if (uiState.mostrarDialogoEditar && uiState.horarioSeleccionado != null) {
            EditarHorarioDialog(
                horario = uiState.horarioSeleccionado!!,
                horasSalidaPorDia = uiState.horasSalidaPorDia,
                horasSalidaDisponibles = horarioViewModel.horasSalidaDisponibles,
                guardando = uiState.guardando,
                onHoraSalidaChange = { dia, hora -> horarioViewModel.actualizarHoraSalida(dia, hora) },
                onGuardar = { horarioViewModel.guardarHorario() },
                onDismiss = { horarioViewModel.ocultarDialogoEditar() },
                onEditarAsignaturas = { dia ->
                    horarioViewModel.cargarAsignaturasParaCurso(uiState.horarioSeleccionado!!.curso)
                    horarioViewModel.mostrarDialogoAsignaturas(dia)
                }
            )
        }
        
        // Diálogo para editar asignaturas del día
        if (uiState.mostrarDialogoAsignaturas && uiState.diaEditando != null) {
            EditarAsignaturasDialog(
                dia = uiState.diaEditando!!,
                bloques = uiState.bloquesPorDia[uiState.diaEditando!!] ?: emptyList(),
                asignaturasDisponibles = uiState.asignaturasDisponibles,
                guardando = uiState.guardando,
                onAgregarBloque = { asig, prof, sala ->
                    horarioViewModel.agregarBloqueAsignatura(uiState.diaEditando!!, asig, prof, sala)
                },
                onEliminarBloque = { bloqueId ->
                    horarioViewModel.eliminarBloqueAsignatura(uiState.diaEditando!!, bloqueId)
                },
                onGuardar = { horarioViewModel.guardarBloquesDelDia(uiState.diaEditando!!) },
                onDismiss = { horarioViewModel.ocultarDialogoAsignaturas() }
            )
        }
        
        // Diálogo de confirmación para reinicializar horarios
        if (mostrarDialogoReinicializar) {
            AlertDialog(
                onDismissRequest = { mostrarDialogoReinicializar = false },
                icon = {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF00897B)
                    )
                },
                title = {
                    Text(
                        "Reinicializar Horarios",
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Column {
                        Text(
                            "Esta acción generará automáticamente las asignaturas para TODOS los cursos según el currículo chileno."
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "• Lenguaje y Matemática todos los días",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            "• Historia, Ciencias e Inglés distribuidos",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Text(
                            "• Artes, Música, Ed. Física y más",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "⚠️ Los horarios existentes serán reemplazados.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF9800),
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            mostrarDialogoReinicializar = false
                            horarioViewModel.reinicializarTodosLosHorariosConAsignaturas()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00897B)
                        )
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reinicializar")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { mostrarDialogoReinicializar = false }
                    ) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}
@Composable
fun HorarioCard(
    horario: HorarioEscolar,
    onClick: () -> Unit
) {
    val colorCurso = when {
        horario.curso.contains("Medio") -> Color(0xFF7B1FA2)
        horario.curso.contains("7°") || horario.curso.contains("8°") -> Color(0xFF1976D2)
        else -> Color(0xFF388E3C)
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header con el nombre del curso
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorCurso)
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Class,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            horario.curso,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Horarios por día
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "🕐 Entrada: 08:00",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Días y horas de salida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DiaSemana.entries.forEach { dia ->
                        val horarioDia = horario.horarioSemanal[dia]
                        val horaSalida = horarioDia?.horaSalida ?: "15:30"
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                dia.abreviatura,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = colorCurso
                            )
                            Text(
                                horaSalida,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.sp
                            )
                            Text(
                                HorariosPredeterminados.calcularDuracionDia(
                                    horarioDia ?: com.example.educanet.model.HorarioDia(dia, "08:00", horaSalida)
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// Nuevo componente con diseño mejorado
@Composable
fun HorarioCardAdmin(
    horario: HorarioEscolar,
    gradientColors: List<Color>,
    onClick: () -> Unit
) {
    val colorCursoGradient = when {
        horario.curso.contains("Medio") -> listOf(Color(0xFF7B1FA2), Color(0xFFAB47BC))
        horario.curso.contains("7°") || horario.curso.contains("8°") -> listOf(Color(0xFF1976D2), Color(0xFF42A5F5))
        else -> listOf(Color(0xFF388E3C), Color(0xFF66BB6A))
    }
    
    val cursoEmoji = when {
        horario.curso.contains("Medio") -> "🎓"
        horario.curso.contains("7°") || horario.curso.contains("8°") -> "📖"
        else -> "📚"
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(colorCursoGradient)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                cursoEmoji,
                                fontSize = 20.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                horario.curso,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "Jornada Completa",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp
                            )
                        }
                    }
                    
                    // Botón de editar
                    Surface(
                        modifier = Modifier.size(36.dp),
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
            
            // Información de entrada
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorCursoGradient[0].copy(alpha = 0.08f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = colorCursoGradient[0],
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Entrada: 08:00 AM",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colorCursoGradient[0]
                )
            }
            
            // Horarios por día con diseño mejorado
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Horarios de Salida",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF636E72)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Días y horas de salida en una fila
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    DiaSemana.entries.forEach { dia ->
                        val horarioDia = horario.horarioSemanal[dia]
                        val horaSalida = horarioDia?.horaSalida ?: "15:30"
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Círculo con día
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = colorCursoGradient[0].copy(alpha = 0.1f)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        dia.abreviatura,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = colorCursoGradient[0]
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                            
                            Text(
                                horaSalida,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                            
                            Text(
                                HorariosPredeterminados.calcularDuracionDia(
                                    horarioDia ?: com.example.educanet.model.HorarioDia(dia, "08:00", horaSalida)
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                fontSize = 10.sp
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
fun EditarHorarioDialog(
    horario: HorarioEscolar,
    horasSalidaPorDia: Map<DiaSemana, String>,
    horasSalidaDisponibles: List<String>,
    guardando: Boolean,
    onHoraSalidaChange: (DiaSemana, String) -> Unit,
    onGuardar: () -> Unit,
    onDismiss: () -> Unit,
    onEditarAsignaturas: (DiaSemana) -> Unit = {}
) {
    Dialog(
        onDismissRequest = { if (!guardando) onDismiss() },
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
                    .verticalScroll(rememberScrollState())
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
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Editar Horario",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                horario.curso,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF00897B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !guardando) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nota sobre la entrada fija
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE8F5E9)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Hora de entrada fija: 08:00 AM",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Selector de hora de salida por día
                Text(
                    "Configurar hora de salida por día:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DiaSemana.entries.forEach { dia ->
                    var expanded by remember { mutableStateOf(false) }
                    val horaSalida = horasSalidaPorDia[dia] ?: "15:30"
                    val numAsignaturas = horario.horarioSemanal[dia]?.bloques?.size ?: 0
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFF5F5F5)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        dia.displayName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        "08:00 - $horaSalida",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                                
                                ExposedDropdownMenuBox(
                                    expanded = expanded,
                                    onExpandedChange = { expanded = !expanded }
                                ) {
                                    OutlinedTextField(
                                        value = horaSalida,
                                        onValueChange = {},
                                        readOnly = true,
                                        modifier = Modifier
                                            .width(120.dp)
                                            .menuAnchor(),
                                        trailingIcon = {
                                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF00897B),
                                            unfocusedBorderColor = Color.Gray.copy(alpha = 0.5f)
                                        ),
                                        textStyle = LocalTextStyle.current.copy(
                                            textAlign = TextAlign.Center,
                                            fontSize = 14.sp
                                        ),
                                        singleLine = true
                                    )
                                    
                                    ExposedDropdownMenu(
                                        expanded = expanded,
                                        onDismissRequest = { expanded = false }
                                    ) {
                                        horasSalidaDisponibles.forEach { hora ->
                                            DropdownMenuItem(
                                                text = { Text(hora) },
                                                onClick = {
                                                    onHoraSalidaChange(dia, hora)
                                                    expanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            // Botón para editar asignaturas del día
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    if (numAsignaturas > 0) "$numAsignaturas asignatura(s)" else "Sin asignaturas",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (numAsignaturas > 0) Color(0xFF4CAF50) else Color.Gray
                                )
                                
                                TextButton(
                                    onClick = { onEditarAsignaturas(dia) },
                                    colors = ButtonDefaults.textButtonColors(
                                        contentColor = Color(0xFF00897B)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Editar Clases", fontSize = 12.sp)
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
                        enabled = !guardando,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00897B)
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = onGuardar,
                        modifier = Modifier.weight(1f),
                        enabled = !guardando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00897B)
                        )
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHorarioState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Schedule,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No hay horarios configurados",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                "Los horarios se crearán automáticamente",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Diálogo para editar las asignaturas de un día específico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditarAsignaturasDialog(
    dia: DiaSemana,
    bloques: List<BloqueHorario>,
    asignaturasDisponibles: List<String>,
    guardando: Boolean,
    onAgregarBloque: (asignatura: String, profesor: String, sala: String) -> Unit,
    onEliminarBloque: (bloqueId: String) -> Unit,
    onGuardar: () -> Unit,
    onDismiss: () -> Unit
) {
    var asignaturaSeleccionada by remember { mutableStateOf("") }
    var profesor by remember { mutableStateOf("") }
    var sala by remember { mutableStateOf("") }
    var expandedAsignatura by remember { mutableStateOf(false) }
    
    // Obtener los bloques de horario disponibles
    val bloquesInfo = remember { HorarioConstantes.generarBloquesDia().filter { it.tipo == TipoBloque.CLASE } }
    val bloquesDisponibles = bloquesInfo.size
    
    Dialog(
        onDismissRequest = { if (!guardando) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                            tint = Color(0xFF00897B),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                "Asignaturas del ${dia.displayName}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "${bloques.size} de $bloquesDisponibles clases asignadas",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF00897B)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, enabled = !guardando) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Formulario para agregar nueva asignatura
                if (bloques.size < bloquesDisponibles) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE8F5E9)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                "Agregar Clase",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Selector de asignatura
                            ExposedDropdownMenuBox(
                                expanded = expandedAsignatura,
                                onExpandedChange = { expandedAsignatura = !expandedAsignatura }
                            ) {
                                OutlinedTextField(
                                    value = asignaturaSeleccionada,
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Asignatura") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAsignatura)
                                    },
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00897B)
                                    )
                                )
                                
                                ExposedDropdownMenu(
                                    expanded = expandedAsignatura,
                                    onDismissRequest = { expandedAsignatura = false }
                                ) {
                                    if (asignaturasDisponibles.isEmpty()) {
                                        DropdownMenuItem(
                                            text = { Text("No hay asignaturas disponibles") },
                                            onClick = { expandedAsignatura = false },
                                            enabled = false
                                        )
                                    } else {
                                        asignaturasDisponibles.forEach { asig ->
                                            DropdownMenuItem(
                                                text = { Text(asig) },
                                                onClick = {
                                                    asignaturaSeleccionada = asig
                                                    expandedAsignatura = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = profesor,
                                    onValueChange = { profesor = it },
                                    label = { Text("Profesor") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00897B)
                                    )
                                )
                                
                                OutlinedTextField(
                                    value = sala,
                                    onValueChange = { sala = it },
                                    label = { Text("Sala") },
                                    modifier = Modifier.width(100.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF00897B)
                                    )
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Info del horario que se asignará
                            val proximoBloque = bloquesInfo.getOrNull(bloques.size)
                            if (proximoBloque != null) {
                                Text(
                                    "Horario: ${proximoBloque.horaInicio} - ${proximoBloque.horaFin} (90 min)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Button(
                                onClick = {
                                    if (asignaturaSeleccionada.isNotEmpty()) {
                                        onAgregarBloque(asignaturaSeleccionada, profesor, sala)
                                        asignaturaSeleccionada = ""
                                        profesor = ""
                                        sala = ""
                                    }
                                },
                                enabled = asignaturaSeleccionada.isNotEmpty(),
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF00897B)
                                )
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Agregar Clase")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Lista de asignaturas asignadas
                Text(
                    "Clases del día:",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                if (bloques.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No hay clases asignadas",
                                color = Color.Gray
                            )
                            Text(
                                "Agrega asignaturas usando el formulario",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(bloques.size) { index ->
                            val bloque = bloques[index]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Número de clase
                                    Surface(
                                        modifier = Modifier.size(36.dp),
                                        shape = CircleShape,
                                        color = Color(0xFF00897B).copy(alpha = 0.15f)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                "${index + 1}",
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF00897B)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            bloque.asignatura,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "${bloque.horaInicio} - ${bloque.horaFin}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF00897B)
                                        )
                                        if (bloque.profesor.isNotEmpty()) {
                                            Text(
                                                "Prof. ${bloque.profesor}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                        if (bloque.sala.isNotEmpty()) {
                                            Text(
                                                "Sala ${bloque.sala}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    
                                    IconButton(
                                        onClick = { onEliminarBloque(bloque.id) }
                                    ) {
                                        Icon(
                                            Icons.Default.Delete,
                                            contentDescription = "Eliminar",
                                            tint = Color(0xFFF44336)
                                        )
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
                        modifier = Modifier.weight(1f),
                        enabled = !guardando,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF00897B)
                        )
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = onGuardar,
                        modifier = Modifier.weight(1f),
                        enabled = !guardando,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00897B)
                        )
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Save,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}