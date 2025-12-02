package com.example.educanet.ui.screens.registro

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.viewmodel.SeleccionarAlumnoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeleccionarAlumnoScreen(
    correoApoderado: String,
    onAlumnoSeleccionado: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: SeleccionarAlumnoViewModel = viewModel()
    
    val alumnos by viewModel.alumnos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()
    val alumnoSeleccionado by viewModel.alumnoSeleccionado.collectAsState()
    val guardadoExitoso by viewModel.guardadoExitoso.collectAsState()
    val errorMensaje by viewModel.errorMensaje.collectAsState()

    // Cargar alumnos al iniciar
    LaunchedEffect(Unit) {
        viewModel.cargarAlumnos()
    }

    // Mostrar mensaje de error
    LaunchedEffect(errorMensaje) {
        if (errorMensaje.isNotEmpty()) {
            Toast.makeText(context, errorMensaje, Toast.LENGTH_LONG).show()
        }
    }

    // Cuando se guarda exitosamente
    LaunchedEffect(guardadoExitoso) {
        if (guardadoExitoso) {
            Toast.makeText(
                context,
                "¡Alumno asignado correctamente! Por favor inicia sesión.",
                Toast.LENGTH_LONG
            ).show()
            onAlumnoSeleccionado()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD),
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopAppBar
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Seleccionar Alumno",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1565C0)
                )
            )

            // Contenido
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Ícono decorativo
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1565C0).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FamilyRestroom,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color(0xFF1565C0)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Vincular con Alumno",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Text(
                    text = "Selecciona el alumno que deseas vincular a tu cuenta de apoderado",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Lista de alumnos
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    if (cargando) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF1565C0))
                        }
                    } else if (alumnos.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonOff,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No hay alumnos registrados",
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.padding(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(alumnos) { alumno ->
                                val isSelected = alumnoSeleccionado?.correo == alumno.correo
                                
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { viewModel.seleccionarAlumno(alumno) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) 
                                            Color(0xFF1565C0).copy(alpha = 0.1f) 
                                        else 
                                            Color(0xFFF5F5F5)
                                    ),
                                    border = if (isSelected) 
                                        androidx.compose.foundation.BorderStroke(2.dp, Color(0xFF1565C0))
                                    else 
                                        null
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Avatar
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) Color(0xFF1565C0)
                                                    else Color(0xFF1976D2).copy(alpha = 0.2f)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = if (isSelected) Color.White else Color(0xFF1976D2),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(16.dp))

                                        // Info del alumno
                                        Column(
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = alumno.nombre,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = if (isSelected) Color(0xFF1565C0) else Color.Black
                                            )
                                            Text(
                                                text = alumno.correo,
                                                fontSize = 14.sp,
                                                color = Color.Gray
                                            )
                                        }

                                        // Check si está seleccionado
                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Seleccionado",
                                                tint = Color(0xFF1565C0),
                                                modifier = Modifier.size(28.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.RadioButtonUnchecked,
                                                contentDescription = null,
                                                tint = Color.Gray,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón confirmar
                Button(
                    onClick = {
                        if (alumnoSeleccionado != null) {
                            viewModel.guardarVinculacion(correoApoderado)
                        } else {
                            Toast.makeText(context, "Por favor selecciona un alumno", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1565C0),
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    enabled = alumnoSeleccionado != null && !cargando
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VINCULAR ALUMNO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Texto de ayuda
                Text(
                    text = "Una vez vinculado, podrás ver el progreso académico de tu hijo/a",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
