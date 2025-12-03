package com.example.educanet.ui.screens.perfil

import android.util.Log
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.educanet.R
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.ui.common.FotoPerfil
import com.example.educanet.viewmodel.ApoderadoViewModel
import com.example.educanet.viewmodel.AlumnoVinculadoInfo
import com.example.educanet.viewmodel.PerfilViewModel
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilApoderadoScreen(
    onLogout: () -> Unit = {},
    onBack: () -> Unit = {},
    perfilViewModel: PerfilViewModel = viewModel(),
    apoderadoViewModel: ApoderadoViewModel = viewModel()
) {
    val uiState by perfilViewModel.uiState.collectAsState()
    val apoderadoState by apoderadoViewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        Log.d("ApoderadoScreen", "Cargando datos iniciales...")
        perfilViewModel.cargarDatosIniciales()
        apoderadoViewModel.cargarDatosApoderado()
    }

    // Auto-refresh cada 15 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            apoderadoViewModel.refreshData()
        }
    }

    // Calcular estadísticas del alumno
    val estadisticas = remember(apoderadoState.notasAlumno) {
        if (apoderadoState.notasAlumno.isEmpty()) {
            EstadisticasAlumno()
        } else {
            EstadisticasAlumno(
                promedio = apoderadoState.notasAlumno.map { it.notas }.average(),
                notaMaxima = apoderadoState.notasAlumno.maxOfOrNull { it.notas } ?: 0.0,
                notaMinima = apoderadoState.notasAlumno.minOfOrNull { it.notas } ?: 0.0,
                totalAsignaturas = apoderadoState.notasAlumno.map { it.asignatura }.distinct().size,
                notasPorAsignatura = apoderadoState.notasAlumno.groupBy { it.asignatura }
                    .mapValues { (_, list) -> list.map { it.notas }.average() }
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con degradado
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFD32F2F),
                            Color(0xFFB71C1C),
                            Color(0xFF880E4F)
                        ),
                        startY = 0f,
                        endY = 600f
                    )
                )
        )
        
        // Fondo con imagen sutil
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.05f
        )

        Column(modifier = Modifier.fillMaxSize()) {
            // TopAppBar transparente
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.FamilyRestroom,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Mi Familia",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 22.sp
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
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Cerrar Sesión",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )

            if (apoderadoState.cargando) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // Header con foto y nombre del apoderado
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Foto de perfil con borde
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .shadow(8.dp, CircleShape)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(3.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!uiState.fotoUrl.isNullOrEmpty()) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(uiState.fotoUrl)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Foto de perfil",
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(50.dp),
                                        tint = Color(0xFFD32F2F)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = uiState.nombre.ifEmpty { apoderadoState.nombreApoderado },
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        "Apoderado Verificado",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Contenido principal con fondo blanco redondeado
                    item {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                            color = Color(0xFFF5F5F5)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp)
                            ) {
                                // Tarjeta del alumno vinculado
                                if (apoderadoState.alumnoVinculado != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(20.dp)
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    Icons.Default.School,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1565C0),
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    "Mi Hijo/a",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1565C0)
                                                )
                                            }
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                // Avatar del alumno
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .clip(CircleShape)
                                                        .background(
                                                            Brush.linearGradient(
                                                                colors = listOf(
                                                                    Color(0xFF1565C0),
                                                                    Color(0xFF42A5F5)
                                                                )
                                                            )
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = apoderadoState.alumnoVinculado!!.nombre
                                                            .split(" ")
                                                            .take(2)
                                                            .mapNotNull { it.firstOrNull()?.uppercase() }
                                                            .joinToString(""),
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 20.sp
                                                    )
                                                }
                                                
                                                Spacer(modifier = Modifier.width(16.dp))
                                                
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = apoderadoState.alumnoVinculado!!.nombre,
                                                        style = MaterialTheme.typography.titleMedium,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                    Text(
                                                        text = apoderadoState.alumnoVinculado!!.correo,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = Color.Gray
                                                    )
                                                }
                                                
                                                Surface(
                                                    shape = CircleShape,
                                                    color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                                ) {
                                                    Icon(
                                                        Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier
                                                            .padding(8.dp)
                                                            .size(24.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } else {
                                    // No hay alumno vinculado
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = Color(0xFFFFF8E1)
                                        ),
                                        border = BorderStroke(1.dp, Color(0xFFFFB300))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(32.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            // Verificar si hay solicitud pendiente
                                            if (apoderadoState.solicitudPendiente != null) {
                                                // Mostrar estado de solicitud pendiente
                                                Box(
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFF2196F3).copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.HourglassTop,
                                                        contentDescription = null,
                                                        tint = Color(0xFF1565C0),
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "Solicitud Pendiente",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF1565C0)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Esperando aprobación para vincular a:",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(12.dp),
                                                    color = Color(0xFF1565C0).copy(alpha = 0.1f)
                                                ) {
                                                    Text(
                                                        text = apoderadoState.solicitudPendiente!!.alumnoNombre,
                                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF1565C0)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                OutlinedButton(
                                                    onClick = { apoderadoViewModel.cancelarSolicitud() },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = Color.Red
                                                    ),
                                                    border = BorderStroke(1.dp, Color.Red)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Cancel,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Cancelar Solicitud")
                                                }
                                            } else {
                                                // No hay solicitud, mostrar opción para crear una
                                                Box(
                                                    modifier = Modifier
                                                        .size(80.dp)
                                                        .clip(CircleShape)
                                                        .background(Color(0xFFFFB300).copy(alpha = 0.2f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        Icons.Default.PersonAdd,
                                                        contentDescription = null,
                                                        tint = Color(0xFFFF8F00),
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Text(
                                                    text = "Sin alumno vinculado",
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFE65100)
                                                )
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Text(
                                                    text = "Envía una solicitud al administrador para vincular a tu hijo/a y poder ver su progreso académico",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = Color.Gray,
                                                    textAlign = TextAlign.Center
                                                )
                                                Spacer(modifier = Modifier.height(16.dp))
                                                Button(
                                                    onClick = { apoderadoViewModel.cargarAlumnosDisponibles() },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color(0xFFFF8F00)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Icon(
                                                        Icons.AutoMirrored.Filled.Send,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text("Solicitar Vinculación")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Mostrar progreso académico si hay alumno vinculado
                    if (apoderadoState.alumnoVinculado != null && apoderadoState.notasAlumno.isNotEmpty()) {
                        // Estadísticas en Surface
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 20.dp)
                                ) {
                                    Spacer(modifier = Modifier.height(20.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Analytics,
                                            contentDescription = null,
                                            tint = Color(0xFFD32F2F),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Rendimiento Académico",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    EstadisticasApoderadoCard(estadisticas)
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.BarChart,
                                            contentDescription = null,
                                            tint = Color(0xFF1565C0),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Progreso por Asignatura",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                    
                                    ProgresoAsignaturasApoderadoCard(estadisticas.notasPorAsignatura)
                                    
                                    Spacer(modifier = Modifier.height(24.dp))
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Assignment,
                                            contentDescription = null,
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Detalle de Notas",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.weight(1f))
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                "${apoderadoState.notasAlumno.size} notas",
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                fontSize = 12.sp,
                                                color = Color(0xFF4CAF50),
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(16.dp))
                                }
                            }
                        }

                        items(apoderadoState.notasAlumno) { nota ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                                    NotaItemApoderado(nota)
                                }
                            }
                        }
                        
                        // Espaciado final
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Spacer(modifier = Modifier.height(32.dp))
                            }
                        }
                    } else if (apoderadoState.alumnoVinculado != null && apoderadoState.notasAlumno.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp)
                                ) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(40.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(100.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFFE3F2FD)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    Icons.AutoMirrored.Filled.MenuBook,
                                                    contentDescription = null,
                                                    tint = Color(0xFF1565C0),
                                                    modifier = Modifier.size(50.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(24.dp))
                                            Text(
                                                text = "Sin notas aún",
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1565C0)
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = "Cuando los profesores registren notas para ${apoderadoState.alumnoVinculado!!.nombre}, aparecerán aquí",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = Color.Gray,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(32.dp))
                                }
                            }
                        }
                    } else {
                        // Sin alumno vinculado - solo mostrar espaciado
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFFF5F5F5)
                            ) {
                                Spacer(modifier = Modifier.height(100.dp))
                            }
                        }
                    }
                }
            }
        }

        // Snackbar para mensajes de éxito
        apoderadoState.successMessage?.let { message ->
            LaunchedEffect(message) {
                delay(3000)
                apoderadoViewModel.limpiarMensajes()
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
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

        // Snackbar para mensajes de error
        apoderadoState.errorMessage?.let { message ->
            LaunchedEffect(message) {
                delay(3000)
                apoderadoViewModel.limpiarMensajes()
            }
            Snackbar(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                containerColor = Color(0xFFF44336)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(message, color = Color.White)
                }
            }
        }
    }

    // Diálogo para seleccionar alumno
    if (apoderadoState.showSolicitudDialog) {
        Dialog(onDismissRequest = { apoderadoViewModel.cerrarDialogoSolicitud() }) {
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
                                Icons.Default.PersonSearch,
                                contentDescription = null,
                                tint = Color(0xFFFF8F00),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Seleccionar Alumno",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(onClick = { apoderadoViewModel.cerrarDialogoSolicitud() }) {
                            Icon(Icons.Default.Close, contentDescription = "Cerrar")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Selecciona el alumno que deseas vincular a tu cuenta:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (apoderadoState.alumnosDisponibles.isEmpty()) {
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
                                    color = Color.Gray
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
                            apoderadoState.alumnosDisponibles.forEach { alumno ->
                                AlumnoSeleccionItem(
                                    alumno = alumno,
                                    onSelect = { 
                                        apoderadoViewModel.enviarSolicitudVinculacion(alumno)
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

data class EstadisticasAlumno(
    val promedio: Double = 0.0,
    val notaMaxima: Double = 0.0,
    val notaMinima: Double = 0.0,
    val totalAsignaturas: Int = 0,
    val notasPorAsignatura: Map<String, Double> = emptyMap()
)

@Composable
fun EstadisticasApoderadoCard(estadisticas: EstadisticasAlumno) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItemApoderado(
                    icon = Icons.Default.Timeline,
                    label = "Promedio",
                    value = String.format(Locale.US, "%.1f", estadisticas.promedio),
                    color = getColorForNotaApoderado(estadisticas.promedio)
                )
                EstadisticaItemApoderado(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Máxima",
                    value = String.format(Locale.US, "%.1f", estadisticas.notaMaxima),
                    color = Color(0xFF4CAF50)
                )
                EstadisticaItemApoderado(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = "Mínima",
                    value = String.format(Locale.US, "%.1f", estadisticas.notaMinima),
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso general
            Text(
                "Progreso General",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))

            val progresoAnimado by animateFloatAsState(
                targetValue = (estadisticas.promedio / 7.0).toFloat().coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 1000),
                label = "progreso"
            )

            LinearProgressIndicator(
                progress = { progresoAnimado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = getColorForNotaApoderado(estadisticas.promedio),
                trackColor = Color(0xFFE0E0E0)
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${(estadisticas.promedio / 7.0 * 100).toInt()}% del máximo (7.0)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}

@Composable
fun EstadisticaItemApoderado(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
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
fun ProgresoAsignaturasApoderadoCard(notasPorAsignatura: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            notasPorAsignatura.forEach { (asignatura, promedio) ->
                AsignaturaProgressItemApoderado(asignatura, promedio)
                if (asignatura != notasPorAsignatura.keys.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AsignaturaProgressItemApoderado(asignatura: String, promedio: Double) {
    val progresoAnimado by animateFloatAsState(
        targetValue = (promedio / 7.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "progreso_$asignatura"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = getColorForNotaApoderado(promedio)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    asignatura,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                String.format(Locale.US, "%.1f", promedio),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getColorForNotaApoderado(promedio)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progresoAnimado },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = getColorForNotaApoderado(promedio),
            trackColor = Color(0xFFE0E0E0)
        )
    }
}

@Composable
fun NotaItemApoderado(nota: ProgresoAcademico) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de nota con color
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(getColorForNotaApoderado(nota.notas).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    String.format(Locale.US, "%.1f", nota.notas),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getColorForNotaApoderado(nota.notas)
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        nota.profesor,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Class,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        nota.curso,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            // Indicador de estado
            Icon(
                imageVector = if (nota.notas >= 4.0) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (nota.notas >= 4.0) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun getColorForNotaApoderado(nota: Double): Color {
    return when {
        nota >= 6.0 -> Color(0xFF4CAF50)  // Verde - Excelente
        nota >= 5.0 -> Color(0xFF8BC34A)  // Verde claro - Muy bueno
        nota >= 4.0 -> Color(0xFFFF9800)  // Naranja - Aprobado
        nota >= 3.0 -> Color(0xFFFF5722)  // Naranja oscuro - Regular
        else -> Color(0xFFF44336)          // Rojo - Reprobado
    }
}

@Composable
fun AlumnoSeleccionItem(
    alumno: AlumnoVinculadoInfo,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
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
            // Avatar del alumno
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1565C0).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                if (alumno.fotoUrl.isNotEmpty()) {
                    AsyncImage(
                        model = alumno.fotoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text(
                        text = alumno.nombre.take(1).uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alumno.nombre,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = alumno.correo,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFFFF8F00)
            )
        }
    }
}