package com.example.educanet.ui.screens.videoApoyo

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.viewmodel.AddVideoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVideoScreen(
    onBack: () -> Unit,
    addVideoViewModel: AddVideoViewModel = viewModel()
) {
    val uiState by addVideoViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de éxito y luego navegar
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            onBack()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
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
                        Text(
                            "Agregar Video",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = MaterialTheme.colorScheme.primary
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
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFE91E63).copy(alpha = 0.15f),
                    modifier = Modifier.size(80.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.VideoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color(0xFFE91E63)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "🎬 Nuevo Video de Apoyo",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    "Comparte contenido educativo con tus alumnos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Card con información del video
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
                            "📝 Información del Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE91E63)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Campo Nombre
                        OutlinedTextField(
                            value = uiState.nombre,
                            onValueChange = addVideoViewModel::onNombreChange,
                            label = { Text("Título del Video") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Title,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFE91E63),
                                focusedLabelColor = Color(0xFFE91E63)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Nivel
                        OutlinedTextField(
                            value = uiState.nivel,
                            onValueChange = addVideoViewModel::onNivelChange,
                            label = { Text("Nivel Educativo") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.School,
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

                        // Campo Descripción
                        OutlinedTextField(
                            value = uiState.descripcion,
                            onValueChange = addVideoViewModel::onDescripcionChange,
                            label = { Text("Descripción") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            minLines = 2,
                            maxLines = 4,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF9C27B0),
                                focusedLabelColor = Color(0xFF9C27B0)
                            )
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Campo Duración
                        OutlinedTextField(
                            value = uiState.duracion.toString(),
                            onValueChange = addVideoViewModel::onDuracionChange,
                            label = { Text("Duración (minutos)") },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFFF9800)
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFFFF9800),
                                focusedLabelColor = Color(0xFFFF9800)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card con URL del video
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
                            "🔗 Enlace del Video",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4CAF50)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            "Pega el enlace de YouTube u otra plataforma",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.videoUrl,
                            onValueChange = addVideoViewModel::onVideoUrlChange,
                            label = { Text("URL del Video") },
                            placeholder = { Text("https://www.youtube.com/watch?v=...") },
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
                        if (uiState.videoUrl.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            val platform = when {
                                uiState.videoUrl.contains("youtube") || uiState.videoUrl.contains("youtu.be") -> "📺 YouTube detectado"
                                uiState.videoUrl.contains("vimeo") -> "🎬 Vimeo detectado"
                                uiState.videoUrl.contains("drive.google") -> "📁 Google Drive detectado"
                                else -> "🔗 Enlace personalizado"
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF4CAF50).copy(alpha = 0.1f)
                            ) {
                                Text(
                                    platform,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF4CAF50)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón guardar
                Button(
                    onClick = { addVideoViewModel.saveVideo() },
                    enabled = !uiState.isSaving && uiState.nombre.isNotBlank() && uiState.videoUrl.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
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
                            Icons.Default.CloudUpload,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Publicar Video",
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
