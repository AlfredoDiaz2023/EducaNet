package com.example.educanet.ui.screens.asistencia

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.educanet.model.Alumno
import com.example.educanet.viewmodel.AsistenciaViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TomarAsistenciaScreen(
    curso: String,
    profesorNombre: String,
    onBack: () -> Unit,
    asistenciaViewModel: AsistenciaViewModel = viewModel()
) {
    val uiState by asistenciaViewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Cargar alumnos del curso
    LaunchedEffect(curso) {
        asistenciaViewModel.seleccionarCurso(curso)
    }

    // Mostrar mensaje
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            asistenciaViewModel.limpiarMensaje()
            if (msg.contains("✅")) {
                // Volver atrás después de guardar exitosamente
                kotlinx.coroutines.delay(1500)
                onBack()
            }
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                "Asistencia - $curso",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50),
                                fontSize = 18.sp
                            )
                            Text(
                                SimpleDateFormat("EEEE, dd 'de' MMMM", Locale("es")).format(Date()),
                                fontSize = 12.sp,
                                color = Color.Gray
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
                // Verificar si ya se tomó asistencia
                if (uiState.yaSeTomoAsistencia) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "⚠️ Ya se registró la asistencia de este curso hoy",
                                color = Color(0xFFE65100),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // Botones de acción rápida
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { asistenciaViewModel.marcarTodosPresentes() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF4CAF50)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF4CAF50))
                        )
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Todos presentes", fontSize = 12.sp)
                    }
                    
                    OutlinedButton(
                        onClick = { asistenciaViewModel.marcarTodosAusentes() },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFF44336)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = true).copy(
                            brush = androidx.compose.ui.graphics.SolidColor(Color(0xFFF44336))
                        )
                    ) {
                        Icon(Icons.Default.Cancel, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Todos ausentes", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Estadísticas rápidas
                val totalAlumnos = uiState.alumnos.size
                val presentes = uiState.asistenciaMap.count { it.value }
                val ausentes = totalAlumnos - presentes

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EstadisticaItem(
                            icon = Icons.Default.People,
                            valor = totalAlumnos.toString(),
                            label = "Total",
                            color = Color(0xFF1565C0)
                        )
                        EstadisticaItem(
                            icon = Icons.Default.CheckCircle,
                            valor = presentes.toString(),
                            label = "Presentes",
                            color = Color(0xFF4CAF50)
                        )
                        EstadisticaItem(
                            icon = Icons.Default.Cancel,
                            valor = ausentes.toString(),
                            label = "Ausentes",
                            color = Color(0xFFF44336)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Lista de alumnos
                if (uiState.cargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFF4CAF50))
                    }
                } else if (uiState.alumnos.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay alumnos inscritos en este curso",
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.alumnos, key = { it.id }) { alumno ->
                            AlumnoAsistenciaItem(
                                alumno = alumno,
                                presente = uiState.asistenciaMap[alumno.id] ?: true,
                                justificacion = uiState.justificacionMap[alumno.id] ?: "",
                                onToggle = { asistenciaViewModel.toggleAsistencia(alumno.id) },
                                onJustificacionChange = { asistenciaViewModel.setJustificacion(alumno.id, it) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(80.dp)) }
                    }
                }

                // Botón guardar
                if (uiState.alumnos.isNotEmpty()) {
                    Button(
                        onClick = { asistenciaViewModel.guardarAsistencia(profesorNombre) },
                        enabled = !uiState.guardando && !uiState.yaSeTomoAsistencia,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (uiState.guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Guardar Asistencia", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EstadisticaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color)
        Text(
            valor,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
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
private fun AlumnoAsistenciaItem(
    alumno: Alumno,
    presente: Boolean,
    justificacion: String,
    onToggle: () -> Unit,
    onJustificacionChange: (String) -> Unit
) {
    var showJustificacionDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (presente) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto del alumno
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(alumno.fotoUrl.ifEmpty { R.drawable.ic_user_placeholder })
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_user_placeholder),
                error = painterResource(R.drawable.ic_user_placeholder),
                contentDescription = "Foto de ${alumno.nombre}",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(2.dp, if (presente) Color(0xFF4CAF50) else Color(0xFFF44336), CircleShape),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre del alumno
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    alumno.nombre,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    alumno.correo,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (!presente && justificacion.isNotEmpty()) {
                    Text(
                        "📝 $justificacion",
                        fontSize = 11.sp,
                        color = Color(0xFFFF9800)
                    )
                }
            }

            // Botón de justificación (solo si está ausente)
            if (!presente) {
                IconButton(
                    onClick = { showJustificacionDialog = true }
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Justificar",
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Checkbox de asistencia
            Checkbox(
                checked = presente,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = Color(0xFF4CAF50),
                    uncheckedColor = Color(0xFFF44336)
                )
            )
        }
    }

    // Diálogo de justificación
    if (showJustificacionDialog) {
        var textoJustificacion by remember { mutableStateOf(justificacion) }
        
        AlertDialog(
            onDismissRequest = { showJustificacionDialog = false },
            title = {
                Text("Justificar ausencia")
            },
            text = {
                Column {
                    Text(
                        "Alumno: ${alumno.nombre}",
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = textoJustificacion,
                        onValueChange = { textoJustificacion = it },
                        label = { Text("Motivo de ausencia") },
                        placeholder = { Text("Ej: Enfermedad, cita médica...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onJustificacionChange(textoJustificacion)
                        showJustificacionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJustificacionDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
