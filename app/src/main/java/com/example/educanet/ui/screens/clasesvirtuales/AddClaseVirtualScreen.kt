package com.example.educanet.ui.screens.clasesvirtuales

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.viewmodel.AddClaseVirtualViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddClaseVirtualScreen(
    onBack: () -> Unit,
    addClaseVirtualViewModel: AddClaseVirtualViewModel = viewModel()
) {
    val uiState by addClaseVirtualViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Resetear estado cuando se abre la pantalla
    LaunchedEffect(Unit) {
        addClaseVirtualViewModel.resetState()
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            snackbarHostState.showSnackbar("¡Clase virtual creada exitosamente!")
            onBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            addClaseVirtualViewModel.clearMessages()
        }
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
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.VideoCall,
                                contentDescription = null,
                                tint = Color(0xFF2196F3),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Nueva Clase",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2196F3)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color(0xFF2196F3)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Icono decorativo
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF2196F3).copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Videocam,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFF2196F3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "📹 Crear Clase Virtual",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Programa una videollamada con tus alumnos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card con información de la clase
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            "📝 Información de la Clase",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2196F3)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Nombre
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = addClaseVirtualViewModel::onNombreChange,
                            label = { Text("Nombre de la Clase") },
                            placeholder = { Text("Ej: Matemáticas - Fracciones") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Title,
                                    contentDescription = null,
                                    tint = Color(0xFF2196F3)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2196F3),
                                focusedLabelColor = Color(0xFF2196F3)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Curso (Dropdown)
                        var cursoExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = cursoExpanded,
                            onExpandedChange = { cursoExpanded = !cursoExpanded }
                        ) {
                            OutlinedTextField(
                                value = uiState.curso.ifEmpty { "Seleccionar curso" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Curso") },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Class,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50)
                                    )
                                },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = cursoExpanded)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    focusedLabelColor = Color(0xFF4CAF50)
                                )
                            )
                            ExposedDropdownMenu(
                                expanded = cursoExpanded,
                                onDismissRequest = { cursoExpanded = false }
                            ) {
                                uiState.cursosDisponibles.forEach { curso ->
                                    DropdownMenuItem(
                                        text = { Text(curso) },
                                        onClick = {
                                            addClaseVirtualViewModel.onCursoChange(curso)
                                            cursoExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.School,
                                                contentDescription = null,
                                                tint = if (curso.contains("Medio")) Color(0xFF9C27B0) else Color(0xFF4CAF50)
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Nivel
                        OutlinedTextField(
                            value = uiState.nivel,
                            onValueChange = addClaseVirtualViewModel::onNivelChange,
                            label = { Text("Nivel Educativo (opcional)") },
                            placeholder = { Text("Ej: 5to Básico") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF9C27B0),
                                focusedLabelColor = Color(0xFF9C27B0)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Descripción
                        OutlinedTextField(
                            value = uiState.descripcion,
                            onValueChange = addClaseVirtualViewModel::onDescripcionChange,
                            label = { Text("Descripción") },
                            placeholder = { Text("¿Qué aprenderán los alumnos?") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9800),
                                focusedLabelColor = Color(0xFFFF9800)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Duración
                        OutlinedTextField(
                            value = if (uiState.duracion == 0) "" else uiState.duracion.toString(),
                            onValueChange = addClaseVirtualViewModel::onDuracionChange,
                            label = { Text("Duración (minutos)") },
                            placeholder = { Text("Ej: 45") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card con URL de la reunión
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Text(
                            "🔗 Enlace de Videoconferencia",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Pega el enlace de Google Meet, Zoom o Teams",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.meetUrl,
                            onValueChange = addClaseVirtualViewModel::onMeetUrlChange,
                            label = { Text("URL de la Reunión") },
                            placeholder = { Text("https://meet.google.com/...") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Link,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF4CAF50),
                                focusedLabelColor = Color(0xFF4CAF50)
                            )
                        )

                        // Indicador de plataforma detectada
                        if (uiState.meetUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(12.dp))
                            val platformInfo = when {
                                uiState.meetUrl.contains("meet.google") -> Triple("📹 Google Meet detectado", Color(0xFF00897B), Icons.Default.VideoCall)
                                uiState.meetUrl.contains("zoom") -> Triple("📹 Zoom detectado", Color(0xFF2D8CFF), Icons.Default.Videocam)
                                uiState.meetUrl.contains("teams") -> Triple("📹 Microsoft Teams detectado", Color(0xFF5059C9), Icons.Default.Groups)
                                else -> Triple("🔗 Enlace personalizado", Color(0xFF757575), Icons.Default.Link)
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = platformInfo.second.copy(alpha = 0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        platformInfo.third,
                                        contentDescription = null,
                                        tint = platformInfo.second,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        platformInfo.first,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = platformInfo.second,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Tips Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFFF3E0)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Consejo",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Para crear un enlace de Google Meet:\n" +
                            "1. Ve a meet.google.com\n" +
                            "2. Clic en 'Nueva reunión'\n" +
                            "3. Copia el enlace y pégalo aquí",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón guardar
                Button(
                    onClick = { addClaseVirtualViewModel.saveClaseVirtual() },
                    enabled = !uiState.isSaving && 
                              uiState.nombre.isNotBlank() && 
                              uiState.meetUrl.isNotBlank() &&
                              uiState.curso.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 6.dp
                    )
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(
                            Icons.Default.VideoCall,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Crear Clase Virtual",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
