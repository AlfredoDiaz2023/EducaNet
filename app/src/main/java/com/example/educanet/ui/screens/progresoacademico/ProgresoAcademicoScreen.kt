package com.example.educanet.ui.screens.progresoacademico

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.delay
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.viewmodel.ProgresoAcademicoViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresoAcademicoScreen(
    rol: String,
    correoAlumno: String? = null,
    onBack: () -> Unit,
    onAddNota: () -> Unit,
    viewModel: ProgresoAcademicoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val notas by viewModel.progresos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val editando by viewModel.editando.collectAsState()
    val mensajeResultado by viewModel.mensajeResultado.collectAsState()
    
    // Estado para el diálogo de edición
    var mostrarDialogoEditar by remember { mutableStateOf(false) }
    var notaSeleccionada by remember { mutableStateOf<ProgresoAcademico?>(null) }
    var nuevaNota by remember { mutableStateOf("") }
    
    // Estado para confirmar eliminación
    var mostrarDialogoEliminar by remember { mutableStateOf(false) }
    
    // Colores del gradiente
    val gradientColors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))
    
    // Correo del profesor actual
    val correoProfesorActual = FirebaseAuth.getInstance().currentUser?.email

    // Decodificar el correo si viene codificado
    val correoDecodificado = remember(correoAlumno) {
        correoAlumno?.let {
            try {
                val decoded = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                Log.d("ProgresoScreen", "Correo decodificado: $decoded")
                decoded
            } catch (e: Exception) {
                Log.e("ProgresoScreen", "Error decodificando correo: ${e.message}")
                it
            }
        }
    }

    // Cargar datos
    LaunchedEffect(correoDecodificado, rol) {
        Log.d("ProgresoScreen", "LaunchedEffect - Correo: $correoDecodificado, Rol: $rol")
        if (rol == "Profesor") {
            viewModel.cargarNotasDelProfesor()
        } else if (!correoDecodificado.isNullOrEmpty()) {
            viewModel.cargarNotasPorCorreo(correoDecodificado, rol)
        } else {
            viewModel.refreshProgresos()
        }
    }

    // Auto-refresh cada 15 segundos
    LaunchedEffect(correoDecodificado, rol) {
        while (true) {
            delay(15000)
            if (rol == "Profesor") {
                viewModel.cargarNotasDelProfesor()
            } else if (!correoDecodificado.isNullOrEmpty()) {
                viewModel.cargarNotasPorCorreo(correoDecodificado, rol)
            } else {
                viewModel.refreshProgresos()
            }
        }
    }
    
    // Mostrar mensaje de resultado
    LaunchedEffect(mensajeResultado) {
        mensajeResultado?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    // Calcular estadísticas
    val estadisticas = remember(notas) {
        if (notas.isEmpty()) {
            EstadisticasAcademicas()
        } else {
            EstadisticasAcademicas(
                promedio = notas.map { it.notas }.average(),
                notaMaxima = notas.maxOfOrNull { it.notas } ?: 0.0,
                notaMinima = notas.minOfOrNull { it.notas } ?: 0.0,
                totalAsignaturas = notas.map { it.asignatura }.distinct().size,
                notasPorAsignatura = notas.groupBy { it.asignatura }
                    .mapValues { (_, list) -> list.map { it.notas }.average() }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        gradientColors[0].copy(alpha = 0.1f),
                        Color(0xFFF5F5F5),
                        Color.White
                    )
                )
            )
    ) {
        // Círculos decorativos
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(gradientColors),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp, bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (rol == "Profesor") {
                                IconButton(onClick = onAddNota) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Agregar nota",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                        
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.White.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    if (rol == "Profesor") Icons.Default.Grade else Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                if (rol == "Profesor") "Mis Calificaciones" else "Mi Progreso Académico",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Text(
                                if (rol == "Profesor") "Notas que has registrado" else "Revisa tu rendimiento",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (rol == "Profesor") {
                    ExtendedFloatingActionButton(
                        onClick = onAddNota,
                        containerColor = gradientColors[0],
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Nueva Nota")
                    }
                }
            }
        ) { padding ->
            if (cargando) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = gradientColors[0])
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando calificaciones...", color = Color.Gray)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    // Estadísticas (para alumnos)
                    if (rol != "Profesor" && notas.isNotEmpty()) {
                        item {
                            EstadisticasCardModerno(estadisticas, gradientColors)
                        }
                        
                        item {
                            SectionTitle(
                                icon = Icons.Default.BarChart,
                                title = "Por Asignatura",
                                color = gradientColors[0]
                            )
                        }
                        
                        item {
                            ProgresoAsignaturasCardModerno(estadisticas.notasPorAsignatura, gradientColors[0])
                        }
                    }
                    
                    item {
                        SectionTitle(
                            icon = Icons.Default.List,
                            title = if (rol == "Profesor") "Notas Registradas" else "Detalle de Notas",
                            color = gradientColors[0]
                        )
                    }
                    
                    if (notas.isEmpty()) {
                        item {
                            EmptyNotasCard(rol, gradientColors[0])
                        }
                    } else {
                        items(notas) { nota ->
                            NotaCardModerno(
                                nota = nota,
                                esProfesor = rol == "Profesor",
                                puedeEditar = rol == "Profesor" && nota.profesorCorreo == correoProfesorActual,
                                onEditar = {
                                    notaSeleccionada = nota
                                    nuevaNota = String.format(Locale.US, "%.1f", nota.notas)
                                    mostrarDialogoEditar = true
                                },
                                onEliminar = {
                                    notaSeleccionada = nota
                                    mostrarDialogoEliminar = true
                                }
                            )
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }
    
    // Diálogo para editar nota
    if (mostrarDialogoEditar && notaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditar = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = gradientColors[0]
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Editar Nota")
                }
            },
            text = {
                Column {
                    Text(
                        "Alumno: ${notaSeleccionada?.alumno}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        "Asignatura: ${notaSeleccionada?.asignatura}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = nuevaNota,
                        onValueChange = { nuevaNota = it },
                        label = { Text("Nueva nota (1.0 - 7.0)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nota = nuevaNota.toDoubleOrNull()
                        if (nota != null && nota in 1.0..7.0) {
                            viewModel.editarNota(notaSeleccionada!!.id, nota)
                            mostrarDialogoEditar = false
                        } else {
                            Toast.makeText(context, "Ingresa una nota válida (1.0 - 7.0)", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = !editando,
                    colors = ButtonDefaults.buttonColors(containerColor = gradientColors[0])
                ) {
                    if (editando) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Guardar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEditar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para confirmar eliminación
    if (mostrarDialogoEliminar && notaSeleccionada != null) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEliminar = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = Color(0xFFF44336)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eliminar Nota")
                }
            },
            text = {
                Text("¿Estás seguro de eliminar la nota de ${notaSeleccionada?.alumno} en ${notaSeleccionada?.asignatura}?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.eliminarNota(notaSeleccionada!!.id)
                        mostrarDialogoEliminar = false
                    },
                    enabled = !editando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    if (editando) {
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
                TextButton(onClick = { mostrarDialogoEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SectionTitle(icon: ImageVector, title: String, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3436)
        )
    }
}

@Composable
fun EmptyNotasCard(rol: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                if (rol == "Profesor") Icons.Default.PostAdd else Icons.Default.School,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = color.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                if (rol == "Profesor") "No has registrado notas aún" else "No hay notas registradas",
                style = MaterialTheme.typography.titleMedium,
                color = Color.Gray
            )
            Text(
                if (rol == "Profesor") "Presiona + para agregar una nueva calificación" else "Tus calificaciones aparecerán aquí",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}

data class EstadisticasAcademicas(
    val promedio: Double = 0.0,
    val notaMaxima: Double = 0.0,
    val notaMinima: Double = 0.0,
    val totalAsignaturas: Int = 0,
    val notasPorAsignatura: Map<String, Double> = emptyMap()
)

@Composable
fun EstadisticasCardModerno(estadisticas: EstadisticasAcademicas, gradientColors: List<Color>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(gradientColors))
                .padding(20.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Analytics,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "📊 Resumen de Rendimiento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    EstadisticaItemModerno(
                        icon = Icons.Default.Timeline,
                        label = "Promedio",
                        value = String.format(Locale.US, "%.1f", estadisticas.promedio),
                        color = getColorForNota(estadisticas.promedio)
                    )
                    EstadisticaItemModerno(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = "Máxima",
                        value = String.format(Locale.US, "%.1f", estadisticas.notaMaxima),
                        color = Color(0xFF4CAF50)
                    )
                    EstadisticaItemModerno(
                        icon = Icons.AutoMirrored.Filled.TrendingDown,
                        label = "Mínima",
                        value = String.format(Locale.US, "%.1f", estadisticas.notaMinima),
                        color = Color(0xFFFF5722)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Barra de progreso
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Progreso General",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            "${(estadisticas.promedio / 7.0 * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val progresoAnimado by animateFloatAsState(
                        targetValue = (estadisticas.promedio / 7.0).toFloat().coerceIn(0f, 1f),
                        animationSpec = tween(durationMillis = 1000),
                        label = "progreso"
                    )
                    
                    LinearProgressIndicator(
                        progress = { progresoAnimado },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = Color.White,
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

@Composable
fun EstadisticaItemModerno(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.White.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun ProgresoAsignaturasCardModerno(notasPorAsignatura: Map<String, Double>, accentColor: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            notasPorAsignatura.forEach { (asignatura, promedio) ->
                AsignaturaProgressItemModerno(asignatura, promedio)
                if (asignatura != notasPorAsignatura.keys.last()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun AsignaturaProgressItemModerno(asignatura: String, promedio: Double) {
    val progresoAnimado by animateFloatAsState(
        targetValue = (promedio / 7.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "progreso_$asignatura"
    )
    val color = getColorForNota(promedio)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = color
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    asignatura,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Text(
                    String.format(Locale.US, "%.1f", promedio),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LinearProgressIndicator(
            progress = { progresoAnimado },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}

@Composable
fun NotaCardModerno(
    nota: ProgresoAcademico,
    esProfesor: Boolean,
    puedeEditar: Boolean,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    val color = getColorForNota(nota.notas)
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de nota
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    String.format(Locale.US, "%.1f", nota.notas),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nota.asignatura,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                
                if (esProfesor) {
                    // Mostrar nombre del alumno para profesores
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            nota.alumno,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Mostrar nombre del profesor para alumnos
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            nota.profesor,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Class,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        nota.curso,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                // Estado (aprobado/reprobado)
                Icon(
                    imageVector = if (nota.notas >= 4.0) Icons.Default.CheckCircle else Icons.Default.Cancel,
                    contentDescription = null,
                    tint = if (nota.notas >= 4.0) Color(0xFF4CAF50) else Color(0xFFF44336),
                    modifier = Modifier.size(24.dp)
                )
                
                // Botones de acción para profesores
                if (esProfesor && puedeEditar) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        IconButton(
                            onClick = onEditar,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color(0xFF1565C0),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onEliminar,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getColorForNota(nota: Double): Color {
    return when {
        nota >= 6.0 -> Color(0xFF4CAF50)
        nota >= 5.0 -> Color(0xFF8BC34A)
        nota >= 4.0 -> Color(0xFFFF9800)
        nota >= 3.0 -> Color(0xFFFF5722)
        else -> Color(0xFFF44336)
    }
}
