package com.example.educanet.ui.screens.menu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.viewmodel.MenuViewModel
import com.example.educanet.viewmodel.PerfilViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MenuScreen(
    nombre: String,
    rol: String,
    fotoUrl: String?,
    onLibroClick: () -> Unit,
    onVideoClick: () -> Unit,
    onClaseVirtualClick: () -> Unit,
    onProgresoAcademicoClick: () -> Unit,
    onVerNotificaciones: () -> Unit,
    onCameraClick: () -> Unit,
    onPerfilClick: () -> Unit = {},
    onProgresoHijoClick: () -> Unit = {},
    onAdminPanelClick: () -> Unit = {},
    onVerResenasClick: () -> Unit = {},
    onAsistenciaClick: () -> Unit = {},
    onVerAsistenciaHijoClick: () -> Unit = {}, // Callback para apoderado
    onVerMiAsistenciaClick: () -> Unit = {}, // Nuevo callback para alumno
    onMisAsignaturasClick: () -> Unit = {}, // Callback para profesor - ver asignaturas
    onVerHorarioClick: () -> Unit = {}, // Callback para ver horario (alumno/apoderado)
    onLogout: () -> Unit,
    menuViewModel: MenuViewModel = viewModel(),
    perfilViewModel: PerfilViewModel = viewModel()
) {
    // Estado para badge de notificaciones
    var hayNotificacionesSinLeer by remember { mutableStateOf(false) }
    val notificacionRepo = remember { NotificacionRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    // Obtener datos del usuario actual
    val auth = FirebaseAuth.getInstance()
    val currentUserEmail = auth.currentUser?.email ?: ""
    var userDocId by remember { mutableStateOf("") }
    var cursoUsuario by remember { mutableStateOf("") }
    val esAdmin = rol.equals("admin", ignoreCase = true) || rol.equals("administrador", ignoreCase = true)
    
    // Cargar datos del usuario para obtener el curso correcto
    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail.isNotEmpty()) {
            try {
                val querySnapshot = FirebaseFirestore.getInstance()
                    .collection("usuario")
                    .whereEqualTo("correo", currentUserEmail)
                    .get()
                    .await()
                
                if (querySnapshot.documents.isNotEmpty()) {
                    val userDoc = querySnapshot.documents[0]
                    userDocId = userDoc.id
                    cursoUsuario = userDoc.getString("curso") ?: ""
                }
            } catch (e: Exception) {
                // Usar valores por defecto
            }
        }
    }
    
    // Estado para diálogo de editar nombre
    var showEditNameDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf(nombre) }
    
    // Estado para diálogo de cambiar foto
    var showPhotoDialog by remember { mutableStateOf(false) }
    
    // Snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Launcher para galería
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            perfilViewModel.onImageSelectedAndSave(it)
        }
    }
    
    // Estado del perfil para obtener la foto actualizada
    val perfilState by perfilViewModel.uiState.collectAsState()
    
    // Cargar datos del perfil al iniciar
    LaunchedEffect(Unit) {
        perfilViewModel.cargarDatosIniciales()
    }
    
    // Usar la foto del ViewModel si está disponible, sino usar la que viene por parámetro
    val fotoActualizada = perfilState.fotoUrl ?: fotoUrl
    
    // Usar el nombre del ViewModel si está disponible
    val nombreActualizado = if (perfilState.nombre.isNotEmpty()) perfilState.nombre else nombre
    
    // Mostrar mensaje de éxito
    LaunchedEffect(perfilState.successMessage) {
        perfilState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            perfilViewModel.clearSuccessMessage()
        }
    }

    // Verificar notificaciones sin leer al cargar (después de obtener datos del usuario)
    LaunchedEffect(userDocId, cursoUsuario) {
        scope.launch {
            hayNotificacionesSinLeer = notificacionRepo.hayNotificacionesSinLeerParaUsuario(
                userId = userDocId,
                esAdmin = esAdmin,
                curso = cursoUsuario
            )
        }
    }

    // Auto-refresh cada 15 segundos
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(15000)
            hayNotificacionesSinLeer = notificacionRepo.hayNotificacionesSinLeerParaUsuario(
                userId = userDocId,
                esAdmin = esAdmin,
                curso = cursoUsuario
            )
            perfilViewModel.cargarDatosIniciales()
        }
    }

    // Colores del gradiente según el rol
    val gradientColors = when (rol) {
        "Administrador" -> listOf(Color(0xFF673AB7), Color(0xFF9C27B0))
        "Profesor" -> listOf(Color(0xFF3F51B5), Color(0xFF5C6BC0))
        "Apoderado" -> listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
        else -> listOf(Color(0xFF667eea), Color(0xFF764ba2)) // Alumno
    }
    
    val rolEmoji = when (rol) {
        "Administrador" -> "👨‍💼"
        "Profesor" -> "👨‍🏫"
        "Apoderado" -> "👨‍👧"
        else -> "🎓" // Alumno
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        gradientColors[0].copy(alpha = 0.15f),
                        Color(0xFFF8F9FA),
                        Color.White
                    )
                )
            )
    ) {
        // Círculos decorativos de fondo
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            gradientColors[0].copy(alpha = 0.2f),
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
                            gradientColors[1].copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            
            // Header Card con gradiente
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(gradientColors)
                        )
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Foto de perfil clickeable
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.clickable { showPhotoDialog = true }
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(if (!fotoActualizada.isNullOrEmpty()) fotoActualizada else R.drawable.ic_user_placeholder)
                                    .crossfade(true)
                                    .build(),
                                placeholder = painterResource(R.drawable.ic_user_placeholder),
                                error = painterResource(R.drawable.ic_user_placeholder),
                                contentDescription = "Foto de perfil",
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )

                            // Icono de editar foto
                            Surface(
                                modifier = Modifier
                                    .size(28.dp)
                                    .align(Alignment.BottomEnd),
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 4.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Editar foto",
                                        tint = gradientColors[0],
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "¡Hola! $rolEmoji",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = nombreActualizado,
                                    color = Color.White,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                IconButton(
                                    onClick = {
                                        nuevoNombre = nombreActualizado
                                        showEditNameDialog = true
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Editar nombre",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = rol,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            
            // Título de sección
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Apps,
                    contentDescription = null,
                    tint = gradientColors[0],
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Menú Principal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )
            }

            // Grid de botones con iconos
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Fila 1: Libros y Videos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuButtonGradient(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        text = "Libros",
                        gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                        onClick = onLibroClick,
                        modifier = Modifier.weight(1f)
                    )
                    MenuButtonGradient(
                        icon = Icons.Default.VideoLibrary,
                        text = "Videos de Apoyo",
                        gradientColors = listOf(Color(0xFF2196F3), Color(0xFF64B5F6)),
                        onClick = onVideoClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 2: Clases Virtuales y Progreso (Progreso no visible para Apoderado)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuButtonGradient(
                        icon = Icons.Default.Videocam,
                        text = "Clases Virtuales",
                        gradientColors = listOf(Color(0xFF9C27B0), Color(0xFFBA68C8)),
                        onClick = onClaseVirtualClick,
                        modifier = Modifier.weight(1f)
                    )
                    // Progreso Académico solo visible para Alumno y Profesor
                    if (rol != "Apoderado") {
                        MenuButtonGradient(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            text = "Progreso Académico",
                            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFB74D)),
                            onClick = onProgresoAcademicoClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Para Apoderado, mostrar botón de Ver Progreso del Hijo
                        MenuButtonGradient(
                            icon = Icons.Default.FamilyRestroom,
                            text = "Progreso de mi Hijo/a",
                            gradientColors = listOf(Color(0xFFFF9800), Color(0xFFFFB74D)),
                            onClick = onProgresoHijoClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Fila 3: Notificaciones y Mi Perfil
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuButtonWithBadgeGradient(
                        icon = Icons.Default.Notifications,
                        text = "Notificaciones",
                        gradientColors = listOf(Color(0xFFF44336), Color(0xFFE57373)),
                        showBadge = hayNotificacionesSinLeer,
                        onClick = {
                            onVerNotificaciones()
                            hayNotificacionesSinLeer = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MenuButtonGradient(
                        icon = Icons.Default.AccountCircle,
                        text = "Mi Perfil",
                        gradientColors = listOf(Color(0xFF607D8B), Color(0xFF90A4AE)),
                        onClick = onPerfilClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 4: Cámara y Ver Reseñas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuButtonGradient(
                        icon = Icons.Default.CameraAlt,
                        text = "Tomar Foto",
                        gradientColors = listOf(Color(0xFF00BCD4), Color(0xFF4DD0E1)),
                        onClick = onCameraClick,
                        modifier = Modifier.weight(1f)
                    )
                    MenuButtonGradient(
                        icon = Icons.Default.RateReview,
                        text = "Ver Reseñas",
                        gradientColors = listOf(Color(0xFFE91E63), Color(0xFFF06292)),
                        onClick = onVerResenasClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 5: Asistencia (solo para profesores)
                if (rol == "Profesor") {
                    MenuButtonGradient(
                        icon = Icons.AutoMirrored.Filled.FactCheck,
                        text = "Control de Asistencia",
                        gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                        onClick = onAsistenciaClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fila: Enseñar Asignatura (solo para profesores)
                if (rol == "Profesor") {
                    MenuButtonGradient(
                        icon = Icons.Default.School,
                        text = "Enseñar Asignatura",
                        gradientColors = listOf(Color(0xFF3F51B5), Color(0xFF7986CB)),
                        onClick = onMisAsignaturasClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fila: Ver Asistencia del hijo (solo para apoderados)
                if (rol == "Apoderado") {
                    MenuButtonGradient(
                        icon = Icons.AutoMirrored.Filled.FactCheck,
                        text = "Ver Asistencia de mi Hijo/a",
                        gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                        onClick = onVerAsistenciaHijoClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fila: Ver Horario del hijo (solo para apoderados)
                if (rol == "Apoderado") {
                    MenuButtonGradient(
                        icon = Icons.Default.CalendarMonth,
                        text = "Horario de mi Hijo/a",
                        gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                        onClick = onVerHorarioClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fila: Ver Mi Asistencia (solo para alumnos)
                if (rol == "Alumno") {
                    MenuButtonGradient(
                        icon = Icons.AutoMirrored.Filled.FactCheck,
                        text = "Ver Mi Asistencia",
                        gradientColors = listOf(Color(0xFF4CAF50), Color(0xFF81C784)),
                        onClick = onVerMiAsistenciaClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fila: Mi Horario (solo para alumnos)
                if (rol == "Alumno") {
                    MenuButtonGradient(
                        icon = Icons.Default.CalendarMonth,
                        text = "Mi Horario",
                        gradientColors = listOf(Color(0xFF667eea), Color(0xFF764ba2)),
                        onClick = onVerHorarioClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Fila 6: Panel de Administración (solo para admin)
                if (rol == "Administrador") {
                    MenuButtonGradient(
                        icon = Icons.Default.AdminPanelSettings,
                        text = "Panel de Administración",
                        gradientColors = listOf(Color(0xFF673AB7), Color(0xFF9575CD)),
                        onClick = onAdminPanelClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de cerrar sesión con diseño mejorado
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onLogout),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFFFF5252), Color(0xFFFF8A80))
                            )
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "Cerrar Sesión",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
    }
    
    // Diálogo para editar nombre
    if (showEditNameDialog) {
        AlertDialog(
            onDismissRequest = { showEditNameDialog = false },
            title = { Text("Editar Nombre") },
            text = {
                OutlinedTextField(
                    value = nuevoNombre,
                    onValueChange = { nuevoNombre = it },
                    label = { Text("Nuevo nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevoNombre.isNotBlank()) {
                            perfilViewModel.actualizarNombre(nuevoNombre.trim())
                            showEditNameDialog = false
                        }
                    },
                    enabled = nuevoNombre.isNotBlank()
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditNameDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
    
    // Diálogo para cambiar foto de perfil
    if (showPhotoDialog) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Cambiar foto de perfil") },
            text = { Text("¿Cómo deseas agregar tu foto?") },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Botón Cámara
                    ElevatedButton(
                        onClick = {
                            showPhotoDialog = false
                            onCameraClick()
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cámara")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Botón Galería
                    ElevatedButton(
                        onClick = {
                            showPhotoDialog = false
                            launcherGaleria.launch("image/*")
                        },
                        colors = ButtonDefaults.elevatedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galería")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun MenuButton(
    icon: ImageVector,
    text: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2
            )
        }
    }
}

// Nuevo componente con gradiente
@Composable
fun MenuButtonGradient(
    icon: ImageVector,
    text: String,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(200f, 200f)
                    )
                )
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun MenuButtonWithBadge(
    icon: ImageVector,
    text: String,
    color: Color,
    showBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )
            }

            // Badge de notificación
            if (showBadge) {
                Badge(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    containerColor = Color.Yellow,
                    contentColor = Color.Black
                ) {
                    Text("!")
                }
            }
        }
    }
}

// Nuevo componente con badge y gradiente
@Composable
fun MenuButtonWithBadgeGradient(
    icon: ImageVector,
    text: String,
    gradientColors: List<Color>,
    showBadge: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(110.dp)
            .shadow(8.dp, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(200f, 200f)
                    )
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = text,
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    textAlign = TextAlign.Center
                )
            }

            // Badge de notificación animado
            if (showBadge) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(22.dp)
                        .background(
                            Color(0xFFFFEB3B),
                            CircleShape
                        )
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "!",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}