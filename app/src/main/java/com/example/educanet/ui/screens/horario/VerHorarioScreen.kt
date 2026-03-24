package com.example.educanet.ui.screens.horario

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.DiaSemana
import com.example.educanet.model.HorarioEscolar
import com.example.educanet.model.HorariosPredeterminados
import com.example.educanet.viewmodel.HorarioViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerHorarioScreen(
    curso: String,
    nombreUsuario: String,
    esApoderado: Boolean = false,
    onBack: () -> Unit,
    horarioViewModel: HorarioViewModel = viewModel()
) {
    val horarioState by horarioViewModel.uiState.collectAsState()
    
    // Colores del gradiente
    val gradientColors = listOf(
        Color(0xFF667eea),
        Color(0xFF764ba2)
    )
    
    // Cargar horario del curso
    LaunchedEffect(curso) {
        horarioViewModel.cargarHorarioPorCurso(curso)
    }
    
    val horario = horarioState.horarioSeleccionado
    val diaActual = obtenerDiaActualEnum()
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF667eea).copy(alpha = 0.1f),
                            Color(0xFFF5F5F5)
                        )
                    )
                )
        )
        
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                // Top Bar con gradiente
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
                                    Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        if (esApoderado) "Horario de mi Hijo/a" else "Mi Horario",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    )
                                    Text(
                                        curso,
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
            if (horarioState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF667eea),
                        strokeWidth = 3.dp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Tarjeta de bienvenida
                    item {
                        WelcomeCard(
                            nombreUsuario = nombreUsuario,
                            curso = curso,
                            esApoderado = esApoderado
                        )
                    }
                    
                    // Información del día actual
                    item {
                        HoyCard(
                            horario = horario,
                            diaActual = diaActual
                        )
                    }
                    
                    // Título de horario semanal
                    item {
                        Text(
                            "📅 Horario Semanal",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    
                    // Tarjetas de cada día
                    items(DiaSemana.entries) { dia ->
                        DiaHorarioCard(
                            dia = dia,
                            horario = horario,
                            esHoy = dia == diaActual
                        )
                    }
                    
                    // Espacio al final
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun WelcomeCard(
    nombreUsuario: String,
    curso: String,
    esApoderado: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF667eea), Color(0xFF764ba2))
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Avatar
                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (esApoderado) Icons.Default.FamilyRestroom else Icons.Default.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (esApoderado) "Horario de" else "¡Hola!",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                    Text(
                        nombreUsuario,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Class,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            curso,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HoyCard(
    horario: HorarioEscolar?,
    diaActual: DiaSemana
) {
    val horarioDia = horario?.horarioSemanal?.get(diaActual)
    val horaEntrada = horarioDia?.horaEntrada ?: "08:00"
    val horaSalida = horarioDia?.horaSalida ?: "15:30"
    val duracion = HorariosPredeterminados.calcularDuracionDia(
        horarioDia ?: com.example.educanet.model.HorarioDia(diaActual, "08:00", "15:30")
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Today,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "Hoy - ${diaActual.displayName}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF4CAF50)
                    )
                    Text(
                        "Jornada Escolar Completa",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Entrada
                TimeInfoBox(
                    icon = Icons.Default.Login,
                    label = "Entrada",
                    time = horaEntrada,
                    color = Color(0xFF2196F3)
                )
                
                // Duración
                TimeInfoBox(
                    icon = Icons.Default.Timer,
                    label = "Duración",
                    time = duracion,
                    color = Color(0xFFFF9800)
                )
                
                // Salida
                TimeInfoBox(
                    icon = Icons.Default.Logout,
                    label = "Salida",
                    time = horaSalida,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}

@Composable
fun TimeInfoBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    time: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            modifier = Modifier.size(50.dp),
            shape = RoundedCornerShape(12.dp),
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(26.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            time,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}

@Composable
fun DiaHorarioCard(
    dia: DiaSemana,
    horario: HorarioEscolar?,
    esHoy: Boolean
) {
    val horarioDia = horario?.horarioSemanal?.get(dia)
    val horaEntrada = horarioDia?.horaEntrada ?: "08:00"
    val horaSalida = horarioDia?.horaSalida ?: "15:30"
    val bloques = horarioDia?.bloques ?: emptyList()
    val duracion = HorariosPredeterminados.calcularDuracionDia(
        horarioDia ?: com.example.educanet.model.HorarioDia(dia, "08:00", "15:30")
    )
    
    // Colores según el día
    val (colorPrimario, colorSecundario) = when (dia) {
        DiaSemana.LUNES -> Pair(Color(0xFF2196F3), Color(0xFF64B5F6))
        DiaSemana.MARTES -> Pair(Color(0xFF9C27B0), Color(0xFFBA68C8))
        DiaSemana.MIERCOLES -> Pair(Color(0xFF4CAF50), Color(0xFF81C784))
        DiaSemana.JUEVES -> Pair(Color(0xFFFF9800), Color(0xFFFFB74D))
        DiaSemana.VIERNES -> Pair(Color(0xFFF44336), Color(0xFFE57373))
    }
    
    val emoji = when (dia) {
        DiaSemana.LUNES -> "🌅"
        DiaSemana.MARTES -> "📚"
        DiaSemana.MIERCOLES -> "🎯"
        DiaSemana.JUEVES -> "⭐"
        DiaSemana.VIERNES -> "🎉"
    }
    
    var expandido by remember { mutableStateOf(esHoy) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (esHoy) Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
                else Modifier.shadow(2.dp, RoundedCornerShape(16.dp))
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esHoy) colorPrimario.copy(alpha = 0.05f) else Color.White
        ),
        border = if (esHoy) androidx.compose.foundation.BorderStroke(2.dp, colorPrimario) else null,
        onClick = { expandido = !expandido }
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Cabecera del día
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador del día
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = colorPrimario.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            emoji,
                            fontSize = 28.sp
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            dia.displayName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorPrimario
                        )
                        if (esHoy) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colorPrimario
                            ) {
                                Text(
                                    " HOY ",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        if (bloques.isNotEmpty()) "${bloques.size} clases" else "Sin clases asignadas",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                
                // Horarios
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            horaEntrada,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = Color(0xFFF44336),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            horaSalida,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF44336)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Icono de expandir
                Icon(
                    if (expandido) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expandido) "Contraer" else "Expandir",
                    tint = colorPrimario
                )
            }
            
            // Contenido expandible - Lista de asignaturas
            AnimatedVisibility(
                visible = expandido,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorPrimario.copy(alpha = 0.03f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    if (bloques.isEmpty()) {
                        // Mensaje cuando no hay asignaturas
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "El administrador aún no ha asignado clases para este día",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        // Mostrar cada asignatura
                        bloques.forEachIndexed { index, bloque ->
                            AsignaturaItem(
                                bloque = bloque,
                                numeroClase = index + 1,
                                colorPrimario = colorPrimario
                            )
                            if (index < bloques.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    color = colorPrimario.copy(alpha = 0.1f)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun AsignaturaItem(
    bloque: com.example.educanet.model.BloqueHorario,
    numeroClase: Int,
    colorPrimario: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Número de clase
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = colorPrimario.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    "$numeroClase",
                    fontWeight = FontWeight.Bold,
                    color = colorPrimario,
                    fontSize = 14.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                bloque.asignatura.ifEmpty { "Asignatura sin nombre" },
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF333333)
            )
            if (bloque.profesor.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        bloque.profesor,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
            if (bloque.sala.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        Icons.Default.Room,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Sala ${bloque.sala}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }
        }
        
        // Horario de la clase
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = colorPrimario.copy(alpha = 0.1f)
            ) {
                Text(
                    "${bloque.horaInicio} - ${bloque.horaFin}",
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = colorPrimario
                )
            }
            Text(
                "${bloque.duracionMinutos} min",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

private fun obtenerDiaActualEnum(): DiaSemana {
    val calendar = Calendar.getInstance()
    return when (calendar.get(Calendar.DAY_OF_WEEK)) {
        Calendar.MONDAY -> DiaSemana.LUNES
        Calendar.TUESDAY -> DiaSemana.MARTES
        Calendar.WEDNESDAY -> DiaSemana.MIERCOLES
        Calendar.THURSDAY -> DiaSemana.JUEVES
        Calendar.FRIDAY -> DiaSemana.VIERNES
        else -> DiaSemana.LUNES // Fin de semana muestra lunes
    }
}
