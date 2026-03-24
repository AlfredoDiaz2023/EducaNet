package com.example.educanet.ui.screens.progresoacademico

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.SistemaNotas
import com.example.educanet.viewmodel.AddProgresoAcademicoViewModel
import com.example.educanet.viewmodel.ModoIngresoNota

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProgresoAcademicoScreen(
    onBack: () -> Unit,
    onVerTablaNotas: () -> Unit = {},
    viewModel: AddProgresoAcademicoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Colores del tema
    val primaryColor = Color(0xFF1565C0)
    val secondaryColor = Color(0xFF42A5F5)
    val accentColor = Color(0xFF4CAF50)
    val gradientColors = listOf(Color(0xFF1565C0), Color(0xFF42A5F5))

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onBack()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

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
                // Header con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.horizontalGradient(gradientColors),
                            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                        )
                        .padding(top = 40.dp, bottom = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = onVerTablaNotas) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = "Ver tabla de notas",
                                    tint = Color.White
                                )
                            }
                        }
                        
                        // Icono central
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Grade,
                                contentDescription = null,
                                modifier = Modifier.size(40.dp),
                                tint = Color.White
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Registrar Calificación",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ingrese los datos de la evaluación",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Card de Información del Profesor
                InfoCard(
                    icon = Icons.Default.School,
                    iconColor = primaryColor,
                    title = "Profesor",
                    value = uiState.profesor.ifEmpty { "Cargando..." },
                    isLoading = uiState.isLoadingProfesor
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Card de Selección de Alumno
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(accentColor.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Seleccionar Alumno",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF333333)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        var expanded by remember { mutableStateOf(false) }
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = if (uiState.alumno.isNotEmpty()) {
                                    "${uiState.alumno}${if (uiState.curso.isNotEmpty()) " • ${uiState.curso}" else ""}"
                                } else "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Toca para seleccionar", color = Color.Gray) },
                                trailingIcon = {
                                    if (uiState.isLoadingAlumnos) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = accentColor
                                        )
                                    } else {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                    focusedContainerColor = Color(0xFFF5F5F5),
                                    unfocusedContainerColor = Color(0xFFF5F5F5)
                                )
                            )
                            
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                if (uiState.alumnos.isEmpty() && !uiState.isLoadingAlumnos) {
                                    DropdownMenuItem(
                                        text = { Text("No hay alumnos registrados") },
                                        onClick = { expanded = false },
                                        enabled = false
                                    )
                                } else {
                                    uiState.alumnos.forEach { alumno ->
                                        DropdownMenuItem(
                                            text = { 
                                                Column {
                                                    Text(
                                                        alumno.nombre, 
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                    Row {
                                                        Text(
                                                            alumno.correo, 
                                                            fontSize = 12.sp,
                                                            color = Color.Gray
                                                        )
                                                        if (alumno.curso.isNotEmpty()) {
                                                            Text(
                                                                " • ${alumno.curso}",
                                                                fontSize = 12.sp,
                                                                color = primaryColor,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            },
                                            onClick = {
                                                viewModel.onAlumnoSelected(alumno)
                                                expanded = false
                                            },
                                            leadingIcon = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape)
                                                        .background(primaryColor.copy(alpha = 0.1f)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = alumno.nombre.take(1).uppercase(),
                                                        color = primaryColor,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Card de Asignatura y Curso
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9800).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Book,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Datos de la Evaluación",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF333333)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = uiState.asignatura,
                            onValueChange = viewModel::onAsignaturaChange,
                            label = { Text("Asignatura") },
                            leadingIcon = {
                                Icon(Icons.Default.MenuBook, contentDescription = null, tint = Color(0xFFFF9800))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9800),
                                focusedLabelColor = Color(0xFFFF9800)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        OutlinedTextField(
                            value = uiState.curso,
                            onValueChange = viewModel::onCursoChange,
                            label = { Text("Curso") },
                            leadingIcon = {
                                Icon(Icons.Default.Class, contentDescription = null, tint = primaryColor)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = uiState.alumno.isEmpty(), // Deshabilitado si hay alumno seleccionado
                            supportingText = {
                                if (uiState.alumno.isNotEmpty() && uiState.curso.isNotEmpty()) {
                                    Text(
                                        "✓ Curso del alumno seleccionado automáticamente",
                                        color = accentColor,
                                        fontSize = 12.sp
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = primaryColor,
                                focusedLabelColor = primaryColor,
                                disabledBorderColor = primaryColor.copy(alpha = 0.5f),
                                disabledTextColor = Color(0xFF333333),
                                disabledLabelColor = primaryColor.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Card de Calificación
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF9C27B0).copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ingresar Calificación",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp,
                                color = Color(0xFF333333)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Selector de modo con diseño mejorado
                        Text(
                            text = "Método de cálculo:",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ModoChip(
                                selected = uiState.modoIngreso == ModoIngresoNota.PORCENTAJE,
                                onClick = { viewModel.onModoIngresoChange(ModoIngresoNota.PORCENTAJE) },
                                icon = Icons.Default.Percent,
                                label = "%",
                                modifier = Modifier.weight(1f)
                            )
                            ModoChip(
                                selected = uiState.modoIngreso == ModoIngresoNota.PUNTAJE,
                                onClick = { viewModel.onModoIngresoChange(ModoIngresoNota.PUNTAJE) },
                                icon = Icons.Default.Numbers,
                                label = "Pts",
                                modifier = Modifier.weight(1f)
                            )
                            ModoChip(
                                selected = uiState.modoIngreso == ModoIngresoNota.NOTA_DIRECTA,
                                onClick = { viewModel.onModoIngresoChange(ModoIngresoNota.NOTA_DIRECTA) },
                                icon = Icons.Default.Edit,
                                label = "Nota",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Campos según el modo de ingreso
                        AnimatedContent(
                            targetState = uiState.modoIngreso,
                            transitionSpec = {
                                fadeIn() + slideInHorizontally() togetherWith fadeOut() + slideOutHorizontally()
                            },
                            label = "modo_ingreso"
                        ) { modo ->
                            when (modo) {
                                ModoIngresoNota.PORCENTAJE -> {
                                    OutlinedTextField(
                                        value = uiState.porcentaje,
                                        onValueChange = viewModel::onPorcentajeChange,
                                        label = { Text("Porcentaje (0-100)") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Percent, null, tint = Color(0xFF9C27B0))
                                        },
                                        suffix = { Text("%") },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF9C27B0),
                                            focusedLabelColor = Color(0xFF9C27B0)
                                        )
                                    )
                                }
                                ModoIngresoNota.PUNTAJE -> {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = uiState.puntajeObtenido,
                                            onValueChange = viewModel::onPuntajeObtenidoChange,
                                            label = { Text("Obtenido") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF9C27B0),
                                                focusedLabelColor = Color(0xFF9C27B0)
                                            )
                                        )
                                        Text(
                                            text = "/",
                                            fontSize = 24.sp,
                                            modifier = Modifier.align(Alignment.CenterVertically)
                                        )
                                        OutlinedTextField(
                                            value = uiState.puntajeTotal,
                                            onValueChange = viewModel::onPuntajeTotalChange,
                                            label = { Text("Total") },
                                            modifier = Modifier.weight(1f),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = Color(0xFF9C27B0),
                                                focusedLabelColor = Color(0xFF9C27B0)
                                            )
                                        )
                                    }
                                }
                                ModoIngresoNota.NOTA_DIRECTA -> {
                                    OutlinedTextField(
                                        value = uiState.notas,
                                        onValueChange = viewModel::onNotasChange,
                                        label = { Text("Nota (1.0 - 7.0)") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Grade, null, tint = Color(0xFF9C27B0))
                                        },
                                        supportingText = { 
                                            Text("Nota aprobatoria: ${SistemaNotas.NOTA_APROBATORIA}") 
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = Color(0xFF9C27B0),
                                            focusedLabelColor = Color(0xFF9C27B0)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Preview de nota calculada con animación
                AnimatedVisibility(
                    visible = uiState.notaCalculada > 0,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (viewModel.esNotaAprobatoria()) 
                                    Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Círculo con la nota
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (viewModel.esNotaAprobatoria())
                                                Color(0xFF4CAF50).copy(alpha = 0.2f)
                                            else
                                                Color(0xFFF44336).copy(alpha = 0.2f)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = String.format("%.1f", uiState.notaCalculada),
                                        fontSize = 36.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (viewModel.esNotaAprobatoria()) 
                                            Color(0xFF2E7D32) else Color(0xFFC62828)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                Text(
                                    text = viewModel.getDescripcionNota(),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF333333)
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = if (viewModel.esNotaAprobatoria()) 
                                            Icons.Default.CheckCircle else Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = if (viewModel.esNotaAprobatoria()) 
                                            Color(0xFF4CAF50) else Color(0xFFF44336),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (viewModel.esNotaAprobatoria()) "APROBADO" else "REPROBADO",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (viewModel.esNotaAprobatoria()) 
                                            Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                }
                                
                                if (uiState.porcentaje.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Porcentaje: ${uiState.porcentaje}%",
                                        fontSize = 14.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Botón de tabla de notas
                OutlinedButton(
                    onClick = onVerTablaNotas,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = primaryColor)
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ver tabla de conversión")
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Botón guardar
                Button(
                    onClick = { viewModel.saveNota() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    enabled = !uiState.isSaving && uiState.notaCalculada > 0 && 
                             uiState.alumno.isNotEmpty() && uiState.asignatura.isNotEmpty()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar Calificación",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun InfoCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    value: String,
    isLoading: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = value,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF333333)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModoChip(
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (selected) Color(0xFF9C27B0) else Color(0xFFF5F5F5)
    val contentColor = if (selected) Color.White else Color.Gray
    
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
