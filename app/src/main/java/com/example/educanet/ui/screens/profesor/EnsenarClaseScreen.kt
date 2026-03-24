package com.example.educanet.ui.screens.profesor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.*
import com.example.educanet.viewmodel.ClaseEnCursoViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnsenarClaseScreen(
    asignatura: String,
    curso: String,
    profesorNombre: String,
    onBack: () -> Unit,
    onFinalizarClase: () -> Unit,
    claseViewModel: ClaseEnCursoViewModel = viewModel()
) {
    val uiState by claseViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    // Estados de la UI
    var tiempoActual by remember { mutableStateOf(obtenerHoraActual()) }
    var tabSeleccionado by remember { mutableIntStateOf(0) }
    var mostrarDialogoCalificacion by remember { mutableStateOf(false) }
    var mostrarDialogoMaterial by remember { mutableStateOf(false) }
    var mostrarDialogoFinalizarClase by remember { mutableStateOf(false) }
    var alumnoParaCalificar by remember { mutableStateOf<AlumnoEnClase?>(null) }
    
    // Iniciar clase al cargar la pantalla
    LaunchedEffect(Unit) {
        claseViewModel.iniciarClase(asignatura, curso, profesorNombre)
    }
    
    // Actualizar reloj cada segundo
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            tiempoActual = obtenerHoraActual()
            claseViewModel.actualizarTiempo()
        }
    }
    
    // Mostrar mensajes
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            claseViewModel.limpiarMensaje()
        }
    }
    
    val colorAsignatura = obtenerColorAsignatura(asignatura)
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(colorAsignatura, colorAsignatura.copy(alpha = 0.7f))
    )
    
    val tabs = listOf(
        TabInfo("Alumnos", Icons.Default.People),
        TabInfo("Asistencia", Icons.Default.CheckCircle),
        TabInfo("Calificaciones", Icons.Default.Grade),
        TabInfo("Material", Icons.Default.Folder)
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.05f
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Clase en Curso",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                "$asignatura • $curso",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
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
                    actions = {
                        // Indicador EN VIVO
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF4CAF50)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    modifier = Modifier.size(8.dp),
                                    shape = CircleShape,
                                    color = Color.White
                                ) {}
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    "EN VIVO",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colorAsignatura,
                        titleContentColor = Color.White
                    )
                )
            },
            bottomBar = {
                // Botón Finalizar Clase
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { mostrarDialogoFinalizarClase = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Stop,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Finalizar Clase",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                // Header con información de la clase y tiempo
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Hora actual
                        InfoChip(
                            icon = Icons.Default.AccessTime,
                            label = "Hora",
                            value = tiempoActual,
                            color = Color(0xFF2196F3)
                        )
                        
                        // Tiempo de clase
                        InfoChip(
                            icon = Icons.Default.Timer,
                            label = "Transcurrido",
                            value = formatearTiempoTranscurrido(uiState.tiempoTranscurrido),
                            color = Color(0xFFFF9800)
                        )
                        
                        // Alumnos
                        InfoChip(
                            icon = Icons.Default.Groups,
                            label = "Alumnos",
                            value = "${uiState.alumnos.size}",
                            color = Color(0xFF9C27B0)
                        )
                    }
                }
                
                // Tabs de navegación
                TabRow(
                    selectedTabIndex = tabSeleccionado,
                    containerColor = Color.White,
                    contentColor = colorAsignatura
                ) {
                    tabs.forEachIndexed { index, tab ->
                        Tab(
                            selected = tabSeleccionado == index,
                            onClick = { tabSeleccionado = index },
                            icon = {
                                Icon(
                                    tab.icon,
                                    contentDescription = tab.title,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            text = {
                                Text(
                                    tab.title,
                                    fontSize = 11.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                    }
                }
                
                // Contenido según tab seleccionado
                when (tabSeleccionado) {
                    0 -> TabAlumnos(
                        alumnos = uiState.alumnos,
                        asistenciaMap = uiState.asistenciaMap,
                        alumnosConectados = uiState.alumnosConectados,
                        colorAsignatura = colorAsignatura
                    )
                    1 -> TabAsistencia(
                        alumnos = uiState.alumnos,
                        asistenciaMap = uiState.asistenciaMap,
                        onToggleAsistencia = { claseViewModel.toggleAsistencia(it) },
                        onMarcarTodosPresentes = { claseViewModel.marcarTodosPresentes() },
                        onMarcarTodosAusentes = { claseViewModel.marcarTodosAusentes() },
                        onGuardarAsistencia = { claseViewModel.guardarAsistencia() },
                        guardando = uiState.guardando,
                        colorAsignatura = colorAsignatura
                    )
                    2 -> TabCalificaciones(
                        alumnos = uiState.alumnos,
                        calificacionesMap = uiState.calificacionesMap,
                        onCalificarAlumno = { alumno ->
                            alumnoParaCalificar = alumno
                            mostrarDialogoCalificacion = true
                        },
                        colorAsignatura = colorAsignatura
                    )
                    3 -> TabMaterial(
                        materiales = uiState.materiales,
                        onAgregarMaterial = { mostrarDialogoMaterial = true },
                        onEliminarMaterial = { claseViewModel.eliminarMaterial(it) },
                        onAbrirMaterial = { url ->
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Handle error
                            }
                        },
                        colorAsignatura = colorAsignatura
                    )
                }
            }
        }
        
        // Loading overlay
        if (uiState.cargando) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = colorAsignatura)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Iniciando clase...", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
    
    // Diálogo para calificar alumno
    if (mostrarDialogoCalificacion && alumnoParaCalificar != null) {
        CalificarAlumnoDialog(
            alumno = alumnoParaCalificar!!,
            notaActual = uiState.calificacionesMap[alumnoParaCalificar!!.alumno.id] ?: 0.0,
            onCalificar = { nota ->
                claseViewModel.actualizarCalificacion(alumnoParaCalificar!!.alumno.id, nota)
                mostrarDialogoCalificacion = false
                alumnoParaCalificar = null
            },
            onDismiss = {
                mostrarDialogoCalificacion = false
                alumnoParaCalificar = null
            },
            colorAsignatura = colorAsignatura
        )
    }
    
    // Diálogo para agregar material
    if (mostrarDialogoMaterial) {
        AgregarMaterialDialog(
            onAgregar = { titulo, descripcion, tipo, url ->
                claseViewModel.agregarMaterial(titulo, descripcion, tipo, url)
                mostrarDialogoMaterial = false
            },
            onDismiss = { mostrarDialogoMaterial = false },
            colorAsignatura = colorAsignatura
        )
    }
    
    // Diálogo para finalizar clase
    if (mostrarDialogoFinalizarClase) {
        FinalizarClaseDialog(
            asignatura = asignatura,
            curso = curso,
            tiempoTranscurrido = uiState.tiempoTranscurrido,
            alumnosPresentes = uiState.asistenciaMap.count { it.value },
            totalAlumnos = uiState.alumnos.size,
            onFinalizar = { guardarAsistencia ->
                claseViewModel.finalizarClase(guardarAsistencia)
                onFinalizarClase()
            },
            onDismiss = { mostrarDialogoFinalizarClase = false },
            colorAsignatura = colorAsignatura
        )
    }
}

// Data class para tabs
private data class TabInfo(val title: String, val icon: ImageVector)

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = color)
        Text(label, fontSize = 11.sp, color = Color.Gray)
    }
}

@Composable
private fun TabAlumnos(
    alumnos: List<AlumnoEnClase>,
    asistenciaMap: Map<String, Boolean>,
    alumnosConectados: List<AlumnoConectado>,
    colorAsignatura: Color
) {
    // Crear set de IDs de alumnos conectados para búsqueda rápida
    val conectadosIds = alumnosConectados.map { it.alumnoId }.toSet()
    val cantidadConectados = conectadosIds.size
    
    if (alumnos.isEmpty()) {
        EmptyState(
            icon = Icons.Default.People,
            mensaje = "No hay alumnos en este curso",
            color = colorAsignatura
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header con contador de conectados
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Lista de Alumnos (${alumnos.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    
                    // Indicador de alumnos conectados
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (cantidadConectados > 0) Color(0xFF4CAF50).copy(alpha = 0.15f) else Color.Gray.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Punto verde animado
                            if (cantidadConectados > 0) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF4CAF50))
                                )
                            }
                            Text(
                                "$cantidadConectados conectados",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (cantidadConectados > 0) Color(0xFF4CAF50) else Color.Gray
                            )
                        }
                    }
                }
            }
            
            items(alumnos) { alumnoEnClase ->
                val presente = asistenciaMap[alumnoEnClase.alumno.id] ?: false
                val estaConectado = conectadosIds.contains(alumnoEnClase.alumno.id)
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            estaConectado -> Color(0xFFE8F5E9) // Verde claro si está conectado
                            presente -> Color.White
                            else -> Color(0xFFFFF3E0) // Naranja claro si está ausente
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar con indicador de conexión
                        Box {
                            Surface(
                                shape = CircleShape,
                                color = if (estaConectado) Color(0xFF4CAF50).copy(alpha = 0.15f) else colorAsignatura.copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Text(
                                        alumnoEnClase.alumno.nombre.take(2).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = if (estaConectado) Color(0xFF4CAF50) else colorAsignatura
                                    )
                                }
                            }
                            
                            // Indicador de conexión en tiempo real (punto verde)
                            if (estaConectado) {
                                Box(
                                    modifier = Modifier
                                        .size(14.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color.White)
                                        .padding(2.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape)
                                            .background(Color(0xFF4CAF50))
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                alumnoEnClase.alumno.nombre,
                                fontWeight = FontWeight.Medium,
                                fontSize = 15.sp
                            )
                            Text(
                                alumnoEnClase.alumno.correo,
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                            
                            // Hora de conexión si está conectado
                            if (estaConectado) {
                                val alumnoConectado = alumnosConectados.find { it.alumnoId == alumnoEnClase.alumno.id }
                                alumnoConectado?.let {
                                    val horaConexion = SimpleDateFormat("HH:mm", Locale("es", "CL")).format(Date(it.horaConexion))
                                    Text(
                                        "Conectado desde las $horaConexion",
                                        fontSize = 11.sp,
                                        color = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                        
                        // Estado de conexión
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = when {
                                estaConectado -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                presente -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                else -> Color(0xFFF44336).copy(alpha = 0.15f)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                if (estaConectado) {
                                    Icon(
                                        Icons.Default.Wifi,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp),
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                                Text(
                                    when {
                                        estaConectado -> "En línea"
                                        presente -> "Presente"
                                        else -> "Ausente"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = when {
                                        estaConectado -> Color(0xFF4CAF50)
                                        presente -> Color(0xFF2196F3)
                                        else -> Color(0xFFF44336)
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
private fun TabAsistencia(
    alumnos: List<AlumnoEnClase>,
    asistenciaMap: Map<String, Boolean>,
    onToggleAsistencia: (String) -> Unit,
    onMarcarTodosPresentes: () -> Unit,
    onMarcarTodosAusentes: () -> Unit,
    onGuardarAsistencia: () -> Unit,
    guardando: Boolean,
    colorAsignatura: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Botones de acción rápida
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = onMarcarTodosPresentes,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4CAF50))
            ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Todos ✓", fontSize = 12.sp)
            }
            
            OutlinedButton(
                onClick = onMarcarTodosAusentes,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFF44336))
            ) {
                Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Todos ✗", fontSize = 12.sp)
            }
            
            Button(
                onClick = onGuardarAsistencia,
                modifier = Modifier.weight(1f),
                enabled = !guardando,
                colors = ButtonDefaults.buttonColors(containerColor = colorAsignatura)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar", fontSize = 12.sp)
                }
            }
        }
        
        // Resumen
        val presentes = asistenciaMap.count { it.value }
        val ausentes = alumnos.size - presentes
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = colorAsignatura.copy(alpha = 0.1f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$presentes", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFF4CAF50))
                    Text("Presentes", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$ausentes", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Color(0xFFF44336))
                    Text("Ausentes", fontSize = 12.sp, color = Color.Gray)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    val porcentaje = if (alumnos.isNotEmpty()) (presentes * 100 / alumnos.size) else 0
                    Text("$porcentaje%", fontWeight = FontWeight.Bold, fontSize = 24.sp, color = colorAsignatura)
                    Text("Asistencia", fontSize = 12.sp, color = Color.Gray)
                }
            }
        }
        
        // Lista de alumnos con checkbox
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(alumnos) { alumnoEnClase ->
                val presente = asistenciaMap[alumnoEnClase.alumno.id] ?: true
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onToggleAsistencia(alumnoEnClase.alumno.id) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (presente) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = presente,
                            onCheckedChange = { onToggleAsistencia(alumnoEnClase.alumno.id) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF4CAF50),
                                uncheckedColor = Color(0xFFF44336)
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Text(
                            alumnoEnClase.alumno.nombre,
                            modifier = Modifier.weight(1f),
                            fontWeight = FontWeight.Medium
                        )
                        
                        Icon(
                            if (presente) Icons.Default.CheckCircle else Icons.Default.Cancel,
                            contentDescription = null,
                            tint = if (presente) Color(0xFF4CAF50) else Color(0xFFF44336)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TabCalificaciones(
    alumnos: List<AlumnoEnClase>,
    calificacionesMap: Map<String, Double>,
    onCalificarAlumno: (AlumnoEnClase) -> Unit,
    colorAsignatura: Color
) {
    if (alumnos.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Grade,
            mensaje = "No hay alumnos para calificar",
            color = colorAsignatura
        )
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFFFF9800)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Toque en un alumno para agregar una calificación rápida durante la clase",
                            fontSize = 13.sp,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
            }
            
            items(alumnos) { alumnoEnClase ->
                val nota = calificacionesMap[alumnoEnClase.alumno.id] ?: 0.0
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCalificarAlumno(alumnoEnClase) },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Avatar
                        Surface(
                            shape = CircleShape,
                            color = colorAsignatura.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                Text(
                                    alumnoEnClase.alumno.nombre.take(2).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    color = colorAsignatura,
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                alumnoEnClase.alumno.nombre,
                                fontWeight = FontWeight.Medium
                            )
                            if (nota > 0) {
                                Text(
                                    "Nota registrada hoy",
                                    fontSize = 12.sp,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                        
                        if (nota > 0) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = obtenerColorNota(nota)
                            ) {
                                Text(
                                    String.format("%.1f", nota),
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = { onCalificarAlumno(alumnoEnClase) },
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calificar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabMaterial(
    materiales: List<MaterialClase>,
    onAgregarMaterial: () -> Unit,
    onEliminarMaterial: (String) -> Unit,
    onAbrirMaterial: (String) -> Unit,
    colorAsignatura: Color
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Botón agregar material
        Button(
            onClick = onAgregarMaterial,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colorAsignatura),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar Material")
        }
        
        if (materiales.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Folder,
                mensaje = "No hay materiales compartidos\nAgrega enlaces, documentos o recursos",
                color = colorAsignatura
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(materiales) { material ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onAbrirMaterial(material.url) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono según tipo
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = obtenerColorTipoMaterial(material.tipo).copy(alpha = 0.15f),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                                    Icon(
                                        obtenerIconoTipoMaterial(material.tipo),
                                        contentDescription = null,
                                        tint = obtenerColorTipoMaterial(material.tipo),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    material.titulo,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (material.descripcion.isNotEmpty()) {
                                    Text(
                                        material.descripcion,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Text(
                                    material.tipo.displayName,
                                    fontSize = 11.sp,
                                    color = obtenerColorTipoMaterial(material.tipo)
                                )
                            }
                            
                            IconButton(onClick = { onEliminarMaterial(material.id) }) {
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
    }
}

@Composable
private fun EmptyState(
    icon: ImageVector,
    mensaje: String,
    color: Color
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = color.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                mensaje,
                textAlign = TextAlign.Center,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun CalificarAlumnoDialog(
    alumno: AlumnoEnClase,
    notaActual: Double,
    onCalificar: (Double) -> Unit,
    onDismiss: () -> Unit,
    colorAsignatura: Color
) {
    var nota by remember { mutableStateOf(if (notaActual > 0) notaActual.toString() else "") }
    var tipoCalificacion by remember { mutableStateOf(TipoCalificacion.PARTICIPACION) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Calificar Alumno",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorAsignatura
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Nombre del alumno
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colorAsignatura.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = colorAsignatura
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            alumno.alumno.nombre,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Input de nota
                OutlinedTextField(
                    value = nota,
                    onValueChange = { 
                        if (it.isEmpty() || it.toDoubleOrNull() != null) {
                            nota = it
                        }
                    },
                    label = { Text("Nota (1.0 - 7.0)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorAsignatura,
                        focusedLabelColor = colorAsignatura
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            nota.toDoubleOrNull()?.let { n ->
                                if (n in 1.0..7.0) {
                                    onCalificar(n)
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAsignatura),
                        enabled = nota.toDoubleOrNull()?.let { it in 1.0..7.0 } ?: false
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AgregarMaterialDialog(
    onAgregar: (String, String, TipoMaterial, String) -> Unit,
    onDismiss: () -> Unit,
    colorAsignatura: Color
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var tipoSeleccionado by remember { mutableStateOf(TipoMaterial.ENLACE) }
    var expandedTipo by remember { mutableStateOf(false) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    "Agregar Material",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = colorAsignatura
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                OutlinedTextField(
                    value = titulo,
                    onValueChange = { titulo = it },
                    label = { Text("Título *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorAsignatura,
                        focusedLabelColor = colorAsignatura
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = { descripcion = it },
                    label = { Text("Descripción") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorAsignatura,
                        focusedLabelColor = colorAsignatura
                    )
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Selector de tipo
                ExposedDropdownMenuBox(
                    expanded = expandedTipo,
                    onExpandedChange = { expandedTipo = !expandedTipo }
                ) {
                    OutlinedTextField(
                        value = tipoSeleccionado.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tipo de material") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorAsignatura,
                            focusedLabelColor = colorAsignatura
                        )
                    )
                    
                    ExposedDropdownMenu(
                        expanded = expandedTipo,
                        onDismissRequest = { expandedTipo = false }
                    ) {
                        TipoMaterial.entries.forEach { tipo ->
                            DropdownMenuItem(
                                text = { Text(tipo.displayName) },
                                onClick = {
                                    tipoSeleccionado = tipo
                                    expandedTipo = false
                                },
                                leadingIcon = {
                                    Icon(
                                        obtenerIconoTipoMaterial(tipo),
                                        contentDescription = null,
                                        tint = obtenerColorTipoMaterial(tipo)
                                    )
                                }
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL del recurso *") },
                    placeholder = { Text("https://...") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorAsignatura,
                        focusedLabelColor = colorAsignatura
                    )
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                            if (titulo.isNotBlank() && url.isNotBlank()) {
                                onAgregar(titulo, descripcion, tipoSeleccionado, url)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = colorAsignatura),
                        enabled = titulo.isNotBlank() && url.isNotBlank()
                    ) {
                        Text("Agregar")
                    }
                }
            }
        }
    }
}

@Composable
private fun FinalizarClaseDialog(
    asignatura: String,
    curso: String,
    tiempoTranscurrido: Long,
    alumnosPresentes: Int,
    totalAlumnos: Int,
    onFinalizar: (guardarAsistencia: Boolean) -> Unit,
    onDismiss: () -> Unit,
    colorAsignatura: Color
) {
    var guardarAsistencia by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFF44336),
                    modifier = Modifier.size(48.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    "¿Finalizar Clase?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Resumen de la clase
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        ResumenItem("Asignatura", asignatura)
                        ResumenItem("Curso", curso)
                        ResumenItem("Duración", formatearTiempoTranscurrido(tiempoTranscurrido))
                        ResumenItem("Asistencia", "$alumnosPresentes / $totalAlumnos alumnos")
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Opción de guardar asistencia
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = guardarAsistencia,
                        onCheckedChange = { guardarAsistencia = it },
                        colors = CheckboxDefaults.colors(checkedColor = colorAsignatura)
                    )
                    Text(
                        "Guardar asistencia al finalizar",
                        modifier = Modifier.clickable { guardarAsistencia = !guardarAsistencia }
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
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
                        onClick = { onFinalizar(guardarAsistencia) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) {
                        Text("Finalizar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumenItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 14.sp)
    }
}

// Funciones auxiliares
private fun obtenerHoraActual(): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date())
}

private fun formatearTiempoTranscurrido(segundos: Long): String {
    val horas = segundos / 3600
    val minutos = (segundos % 3600) / 60
    val segs = segundos % 60
    return if (horas > 0) {
        String.format("%02d:%02d:%02d", horas, minutos, segs)
    } else {
        String.format("%02d:%02d", minutos, segs)
    }
}

private fun obtenerColorAsignatura(asignatura: String): Color {
    return when {
        asignatura.contains("Lenguaje") || asignatura.contains("Literatura") -> Color(0xFFE53935)
        asignatura.contains("Matemática") -> Color(0xFF1E88E5)
        asignatura.contains("Historia") -> Color(0xFF8D6E63)
        asignatura.contains("Ciencias") -> Color(0xFF43A047)
        asignatura.contains("Inglés") -> Color(0xFF5E35B1)
        asignatura.contains("Educación Física") -> Color(0xFFFF8F00)
        asignatura.contains("Arte") || asignatura.contains("Música") -> Color(0xFFD81B60)
        asignatura.contains("Tecnología") -> Color(0xFF00ACC1)
        asignatura.contains("Filosofía") -> Color(0xFF6D4C41)
        asignatura.contains("Ciudadana") -> Color(0xFF7CB342)
        else -> Color(0xFF3F51B5)
    }
}

private fun obtenerColorNota(nota: Double): Color {
    return when {
        nota >= 6.0 -> Color(0xFF4CAF50)
        nota >= 5.0 -> Color(0xFF8BC34A)
        nota >= 4.0 -> Color(0xFFFFC107)
        nota >= 3.0 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }
}

private fun obtenerIconoTipoMaterial(tipo: TipoMaterial): ImageVector {
    return when (tipo) {
        TipoMaterial.DOCUMENTO -> Icons.Default.Description
        TipoMaterial.IMAGEN -> Icons.Default.Image
        TipoMaterial.VIDEO -> Icons.Default.VideoLibrary
        TipoMaterial.PRESENTACION -> Icons.Default.Slideshow
        TipoMaterial.ENLACE -> Icons.Default.Link
        TipoMaterial.OTRO -> Icons.Default.AttachFile
    }
}

private fun obtenerColorTipoMaterial(tipo: TipoMaterial): Color {
    return when (tipo) {
        TipoMaterial.DOCUMENTO -> Color(0xFF1E88E5)
        TipoMaterial.IMAGEN -> Color(0xFF43A047)
        TipoMaterial.VIDEO -> Color(0xFFE53935)
        TipoMaterial.PRESENTACION -> Color(0xFFFF9800)
        TipoMaterial.ENLACE -> Color(0xFF5E35B1)
        TipoMaterial.OTRO -> Color(0xFF757575)
    }
}
