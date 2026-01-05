package com.example.educanet.ui.screens.asistencia

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.viewmodel.AsistenciaViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerAsistenciaAlumnoScreen(
    alumnoId: String,
    alumnoNombre: String,
    onBack: () -> Unit,
    asistenciaViewModel: AsistenciaViewModel = viewModel()
) {
    val uiState by asistenciaViewModel.uiState.collectAsState()

    LaunchedEffect(alumnoId) {
        Log.d("VerAsistenciaScreen", "Cargando asistencia para: $alumnoNombre (ID: $alumnoId)")
        asistenciaViewModel.cargarAsistenciaAlumno(alumnoId)
    }
    
    // Auto-refresh cada 15 segundos
    LaunchedEffect(alumnoId) {
        while (true) {
            delay(15000)
            asistenciaViewModel.refreshAsistenciaAlumno(alumnoId)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.06f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Asistencia",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                            Text(
                                alumnoNombre,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color(0xFF1565C0)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Card con porcentaje de asistencia
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when {
                            uiState.porcentajeAsistencia >= 90 -> Color(0xFFE8F5E9)
                            uiState.porcentajeAsistencia >= 75 -> Color(0xFFFFF8E1)
                            else -> Color(0xFFFFEBEE)
                        }
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            when {
                                uiState.porcentajeAsistencia >= 90 -> Icons.Default.EmojiEvents
                                uiState.porcentajeAsistencia >= 75 -> Icons.Default.Warning
                                else -> Icons.Default.Error
                            },
                            contentDescription = null,
                            tint = when {
                                uiState.porcentajeAsistencia >= 90 -> Color(0xFF4CAF50)
                                uiState.porcentajeAsistencia >= 75 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            },
                            modifier = Modifier.size(48.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "${String.format("%.1f", uiState.porcentajeAsistencia)}%",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                uiState.porcentajeAsistencia >= 90 -> Color(0xFF4CAF50)
                                uiState.porcentajeAsistencia >= 75 -> Color(0xFFFF9800)
                                else -> Color(0xFFF44336)
                            }
                        )
                        
                        Text(
                            "Porcentaje de Asistencia",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Estadísticas
                        val totalClases = uiState.historialAsistencia.size
                        val presentes = uiState.historialAsistencia.count { it.presente }
                        val ausentes = totalClases - presentes
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    totalClases.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF1565C0)
                                )
                                Text("Total", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    presentes.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFF4CAF50)
                                )
                                Text("Presentes", fontSize = 12.sp, color = Color.Gray)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    ausentes.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color(0xFFF44336)
                                )
                                Text("Ausentes", fontSize = 12.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "📅 Historial de Asistencia",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (uiState.cargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF1565C0))
                    }
                } else if (uiState.historialAsistencia.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay registros de asistencia",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "El profesor aún no ha registrado\nasistencia para este alumno",
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.historialAsistencia) { asistencia ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (asistencia.presente) 
                                        Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
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
                                        if (asistencia.presente) Icons.Default.CheckCircle 
                                        else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (asistencia.presente) Color(0xFF4CAF50) 
                                               else Color(0xFFF44336),
                                        modifier = Modifier.size(32.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("es"))
                                                .format(Date(asistencia.fecha)),
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF333333)
                                        )
                                        Text(
                                            "Curso: ${asistencia.curso}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        Text(
                                            "Profesor: ${asistencia.profesorNombre}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                        if (!asistencia.presente && asistencia.justificacion.isNotEmpty()) {
                                            Text(
                                                "📝 ${asistencia.justificacion}",
                                                fontSize = 11.sp,
                                                color = Color(0xFFFF9800)
                                            )
                                        }
                                    }
                                    
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (asistencia.presente) 
                                            Color(0xFF4CAF50) else Color(0xFFF44336)
                                    ) {
                                        Text(
                                            if (asistencia.presente) "Presente" else "Ausente",
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                            fontSize = 12.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        
                        item { Spacer(modifier = Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}
