package com.example.educanet.ui.screens.asistencia

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.example.educanet.model.Cursos
import com.example.educanet.viewmodel.AsistenciaViewModel
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsistenciaScreen(
    onBack: () -> Unit,
    onTomarAsistencia: (String) -> Unit, // Navega con el curso seleccionado
    asistenciaViewModel: AsistenciaViewModel = viewModel()
) {
    val uiState by asistenciaViewModel.uiState.collectAsState()
    var cursoExpandido by remember { mutableStateOf(false) }
    var cursoSeleccionado by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        asistenciaViewModel.cargarHistorialProfesor()
    }
    
    // Auto-refresh cada 15 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            asistenciaViewModel.refreshHistorialProfesor()
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.AutoMirrored.Filled.FactCheck,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Control de Asistencia",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color(0xFF4CAF50)
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
                // Card para seleccionar curso
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            "📋 Tomar Asistencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        // Dropdown de cursos
                        ExposedDropdownMenuBox(
                            expanded = cursoExpandido,
                            onExpandedChange = { cursoExpandido = !cursoExpandido }
                        ) {
                            OutlinedTextField(
                                value = cursoSeleccionado.ifEmpty { "Seleccionar curso" },
                                onValueChange = {},
                                readOnly = true,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = cursoExpandido)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    focusedLabelColor = Color(0xFF4CAF50)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = cursoExpandido,
                                onDismissRequest = { cursoExpandido = false }
                            ) {
                                Cursos.lista.forEach { curso ->
                                    DropdownMenuItem(
                                        text = { Text(curso) },
                                        onClick = {
                                            cursoSeleccionado = curso
                                            cursoExpandido = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.Class,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Botón para ir a tomar asistencia
                        Button(
                            onClick = {
                                if (cursoSeleccionado.isNotEmpty()) {
                                    onTomarAsistencia(cursoSeleccionado)
                                }
                            },
                            enabled = cursoSeleccionado.isNotEmpty(),
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Iniciar Asistencia", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Título historial
                Text(
                    "📊 Historial Reciente",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Historial de asistencias
                if (uiState.cargando) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                } else if (uiState.historialAsistencia.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF8E1)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFFFF8F00)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "No hay registros de asistencia aún",
                                color = Color(0xFFFF8F00)
                            )
                        }
                    }
                } else {
                    // Agrupar por fecha y curso
                    val historialAgrupado = uiState.historialAsistencia
                        .groupBy { 
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                .format(Date(it.fecha)) + " - " + it.curso 
                        }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        historialAgrupado.forEach { (grupo, asistencias) ->
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color.White),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                grupo,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1565C0)
                                            )
                                            val presentes = asistencias.count { it.presente }
                                            val total = asistencias.size
                                            Surface(
                                                shape = RoundedCornerShape(20.dp),
                                                color = if (presentes == total) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                                            ) {
                                                Text(
                                                    "$presentes/$total presentes",
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                    fontSize = 12.sp,
                                                    color = if (presentes == total) Color(0xFF4CAF50) else Color(0xFFFF9800)
                                                )
                                            }
                                        }
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
