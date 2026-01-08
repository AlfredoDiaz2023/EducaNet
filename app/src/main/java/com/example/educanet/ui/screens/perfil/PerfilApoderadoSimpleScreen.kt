package com.example.educanet.ui.screens.perfil

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.ui.common.FotoPerfil
import com.example.educanet.viewmodel.ApoderadoViewModel
import com.example.educanet.viewmodel.PerfilViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilApoderadoSimpleScreen(
    onLogout: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onBack: () -> Unit = {},
    capturedImageUri: Uri? = null,
    perfilViewModel: PerfilViewModel = viewModel(),
    apoderadoViewModel: ApoderadoViewModel = viewModel()
) {
    val uiState by perfilViewModel.uiState.collectAsState()
    val apoderadoState by apoderadoViewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Colores del gradiente para apoderado (rojo)
    val gradientColors = listOf(Color(0xFFD32F2F), Color(0xFFEF5350))
    
    // Snackbar para mensajes
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        Log.d("PerfilApoderadoSimple", "Cargando datos iniciales...")
        perfilViewModel.cargarDatosIniciales()
        apoderadoViewModel.cargarDatosApoderado()
    }
    
    // Auto-refresh cada 15 segundos
    LaunchedEffect(Unit) {
        while (true) {
            delay(15000)
            perfilViewModel.refreshPerfil()
            apoderadoViewModel.cargarDatosApoderado()
        }
    }

    // Procesar imagen capturada de la cámara
    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uri ->
            Log.d("PerfilApoderadoSimple", "Imagen capturada recibida: $uri")
            perfilViewModel.onImageSelectedAndSave(uri)
        }
    }
    
    // Mostrar mensaje de éxito
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            perfilViewModel.clearSuccessMessage()
        }
    }
    
    // Mostrar mensaje de error
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            perfilViewModel.clearErrorMessage()
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
        
        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 50.dp, y = 100.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            gradientColors[1].copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(gradientColors),
                            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 60.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White
                                )
                            }
                            Text(
                                "Mi Perfil",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(48.dp))
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Foto de perfil superpuesta
                Box(
                    modifier = Modifier
                        .offset(y = (-40).dp)
                        .shadow(8.dp, CircleShape)
                ) {
                    FotoPerfil(
                        fotoUrl = uiState.fotoUrl,
                        isUploading = uiState.isUploading,
                        onImageSelected = { uri -> perfilViewModel.onImageSelectedAndSave(uri) },
                        onCameraClick = onCameraClick
                    )
                }

                // Badge de rol
                Surface(
                    modifier = Modifier.offset(y = (-30).dp),
                    shape = RoundedCornerShape(20.dp),
                    color = gradientColors[0].copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.SupervisorAccount,
                            contentDescription = null,
                            tint = gradientColors[0],
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Apoderado",
                            color = gradientColors[0],
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Card de información principal
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(8.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        PerfilInfoItem(
                            icon = Icons.Default.Person,
                            label = "Nombre",
                            value = uiState.nombre.ifEmpty { apoderadoState.nombreApoderado.ifEmpty { "Cargando..." } },
                            color = gradientColors[0]
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.Gray.copy(alpha = 0.1f)
                        )
                        
                        PerfilInfoItem(
                            icon = Icons.Default.Email,
                            label = "Correo electrónico",
                            value = uiState.usuario?.correo ?: "No disponible",
                            color = gradientColors[0]
                        )
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.Gray.copy(alpha = 0.1f)
                        )
                        
                        PerfilInfoItem(
                            icon = Icons.Default.Badge,
                            label = "Rol en el sistema",
                            value = "Apoderado",
                            color = gradientColors[0]
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Card de alumno vinculado
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
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
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        Color(0xFF1565C0).copy(alpha = 0.15f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF1565C0),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Alumno Vinculado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1565C0)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (apoderadoState.alumnoVinculado != null) {
                            PerfilInfoItem(
                                icon = Icons.Default.Person,
                                label = "Nombre del alumno",
                                value = apoderadoState.alumnoVinculado?.nombre ?: "No disponible",
                                color = Color(0xFF1565C0)
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            PerfilInfoItem(
                                icon = Icons.Default.Email,
                                label = "Correo del alumno",
                                value = apoderadoState.alumnoVinculado?.correo ?: "No disponible",
                                color = Color(0xFF1565C0)
                            )
                        } else if (apoderadoState.cargando) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color(0xFF1565C0),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    "Cargando información...",
                                    color = Color.Gray
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "No hay alumno vinculado",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Botón de cerrar sesión
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Cerrar Sesión",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
