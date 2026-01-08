package com.example.educanet.ui.screens.notificaciones

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.Notificacion
import com.example.educanet.model.TipoNotificacion
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.viewmodel.UnirseClaseViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(
    onBack: () -> Unit,
    unirseClaseViewModel: UnirseClaseViewModel = viewModel()
) {
    val repo = remember { NotificacionRepository() }
    val scope = rememberCoroutineScope()
    var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Obtener usuario actual y su rol
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val currentUserId = currentUser?.uid ?: ""
    val currentUserEmail = currentUser?.email ?: ""
    var esAdmin by remember { mutableStateOf(false) }
    var cursoAlumno by remember { mutableStateOf("") }
    var nombreAlumno by remember { mutableStateOf("") }
    var userDocId by remember { mutableStateOf("") }
    var datosUsuarioCargados by remember { mutableStateOf(false) }
    
    // Estado del ViewModel para unirse a clase
    val unirseClaseState by unirseClaseViewModel.uiState.collectAsState()
    
    // Determinar si el usuario es admin y obtener datos del alumno
    LaunchedEffect(currentUserEmail) {
        if (currentUserEmail.isNotEmpty()) {
            try {
                // Buscar por correo en lugar de por UID
                val querySnapshot = FirebaseFirestore.getInstance()
                    .collection("usuario")
                    .whereEqualTo("correo", currentUserEmail)
                    .get()
                    .await()
                
                if (querySnapshot.documents.isNotEmpty()) {
                    val userDoc = querySnapshot.documents[0]
                    userDocId = userDoc.id
                    val rol = userDoc.getString("rol") ?: ""
                    esAdmin = rol.equals("admin", ignoreCase = true) || rol.equals("administrador", ignoreCase = true)
                    cursoAlumno = userDoc.getString("curso") ?: ""
                    nombreAlumno = userDoc.getString("nombre") ?: ""
                    datosUsuarioCargados = true
                    
                    android.util.Log.d("NotificacionesScreen", "📋 Usuario cargado:")
                    android.util.Log.d("NotificacionesScreen", "   Email: $currentUserEmail")
                    android.util.Log.d("NotificacionesScreen", "   DocID: $userDocId")
                    android.util.Log.d("NotificacionesScreen", "   Rol: $rol")
                    android.util.Log.d("NotificacionesScreen", "   Curso: '$cursoAlumno'")
                    android.util.Log.d("NotificacionesScreen", "   Nombre: $nombreAlumno")
                    
                    // Si es alumno, cargar clases activas de su curso
                    if (rol.equals("Alumno", ignoreCase = true) && cursoAlumno.isNotEmpty()) {
                        unirseClaseViewModel.cargarClasesActivas(cursoAlumno)
                    }
                    
                    // Cargar notificaciones inmediatamente después de obtener datos
                    android.util.Log.d("NotificacionesScreen", "📥 Cargando notificaciones...")
                    notificaciones = if (esAdmin) {
                        repo.obtenerNotificacionesParaAdmin()
                    } else {
                        repo.obtenerNotificacionesPorUsuario(userDocId, cursoAlumno)
                    }
                    android.util.Log.d("NotificacionesScreen", "✅ Notificaciones cargadas: ${notificaciones.size}")
                    notificaciones.forEach { n ->
                        android.util.Log.d("NotificacionesScreen", "   - ${n.titulo} (tipo: ${n.tipoNotificacion}, curso: '${n.curso}')")
                    }
                    
                    repo.marcarTodasComoLeidas()
                } else {
                    android.util.Log.e("NotificacionesScreen", "No se encontró usuario con correo: $currentUserEmail")
                    datosUsuarioCargados = true
                }
                cargando = false
            } catch (e: Exception) {
                android.util.Log.e("NotificacionesScreen", "Error cargando datos", e)
                esAdmin = false
                datosUsuarioCargados = true
                cargando = false
            }
        }
    }

    // Auto-refresh cada 15 segundos (solo después de que los datos estén cargados)
    LaunchedEffect(datosUsuarioCargados) {
        if (datosUsuarioCargados) {
            while (true) {
                delay(15000) // 15 segundos
                if (userDocId.isNotEmpty()) {
                    notificaciones = if (esAdmin) {
                        repo.obtenerNotificacionesParaAdmin()
                    } else {
                        repo.obtenerNotificacionesPorUsuario(userDocId, cursoAlumno)
                    }
                    // Refrescar clases activas si es alumno
                    if (cursoAlumno.isNotEmpty()) {
                        unirseClaseViewModel.cargarClasesActivas(cursoAlumno)
                    }
                }
            }
        }
    }
    
    // Mostrar mensajes del ViewModel
    LaunchedEffect(unirseClaseState.mensaje) {
        unirseClaseState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            unirseClaseViewModel.limpiarMensaje()
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
                        Text(
                            "Notificaciones",
                            fontWeight = FontWeight.Bold
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
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
                // Info de depuración (mostrar curso del alumno)
                if (!cargando && cursoAlumno.isNotEmpty() && !esAdmin) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Info,
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tu curso: $cursoAlumno",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF1976D2)
                            )
                        }
                    }
                }
                
                // Contador de notificaciones
                if (!cargando && notificaciones.isNotEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
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
                                Icons.Default.Notifications,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Tienes ${notificaciones.size} notificaciones",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                if (cargando) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (notificaciones.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay notificaciones",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notificaciones, key = { it.id }) { noti ->
                            NotificacionItem(
                                notificacion = noti,
                                onDelete = {
                                    scope.launch {
                                        repo.eliminarNotificacion(noti.id)
                                        notificaciones = notificaciones.filter { it.id != noti.id }
                                    }
                                },
                                onUnirseClase = { claseId, notificacionId ->
                                    scope.launch {
                                        unirseClaseViewModel.unirseAClase(
                                            claseId = claseId,
                                            alumnoId = userDocId,
                                            alumnoNombre = nombreAlumno,
                                            notificacionId = notificacionId
                                        )
                                        // Actualizar la notificación localmente
                                        notificaciones = notificaciones.map { 
                                            if (it.id == notificacionId) {
                                                it.copy(accionRealizada = true)
                                            } else it
                                        }
                                    }
                                },
                                uniendose = unirseClaseState.uniendose
                            )
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificacionItem(
    notificacion: Notificacion,
    onDelete: () -> Unit,
    onUnirseClase: (String, String) -> Unit = { _, _ -> },
    uniendose: Boolean = false
) {
    val iconoYColor = getIconoNotificacion(notificacion)
    val esNotificacionClase = notificacion.tipoNotificacion == TipoNotificacion.CLASE_INICIADA.name
    val puedeUnirse = esNotificacionClase && notificacion.accionable && !notificacion.accionRealizada
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = if (esNotificacionClase && puedeUnirse) 4.dp else 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (esNotificacionClase && puedeUnirse) 
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icono de la notificación
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = RoundedCornerShape(10.dp),
                    color = iconoYColor.second.copy(alpha = 0.2f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = iconoYColor.first,
                            contentDescription = null,
                            tint = iconoYColor.second,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            notificacion.titulo,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        if (esNotificacionClase && puedeUnirse) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF4CAF50)
                            ) {
                                Text(
                                    "EN VIVO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        notificacion.mensaje,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Mostrar info adicional para notificaciones de clase
                    if (esNotificacionClase && notificacion.profesorNombre.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Prof. ${notificacion.profesorNombre}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        formatearFecha(notificacion.fecha),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            
            // Botón de unirse a clase
            if (esNotificacionClase) {
                Spacer(modifier = Modifier.height(12.dp))
                
                if (puedeUnirse) {
                    Button(
                        onClick = { onUnirseClase(notificacion.claseId, notificacion.id) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uniendose,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        if (uniendose) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        } else {
                            Icon(
                                Icons.AutoMirrored.Filled.Login,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            if (uniendose) "Uniéndose..." else "Unirse a la Clase",
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else if (notificacion.accionRealizada) {
                    // Ya se unió
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "¡Ya estás presente en esta clase!",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF4CAF50),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    // Clase ya no activa
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Cancel,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Esta clase ya finalizó",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getIconoNotificacion(notificacion: Notificacion): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
    // Primero verificar por tipo de notificación
    when (notificacion.tipoNotificacion) {
        TipoNotificacion.CLASE_INICIADA.name -> return Pair(Icons.Default.Videocam, Color(0xFF9C27B0))
        TipoNotificacion.TAREA.name -> return Pair(Icons.AutoMirrored.Filled.Assignment, Color(0xFFFF9800))
        TipoNotificacion.CALIFICACION.name -> return Pair(Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFFF5722))
        TipoNotificacion.ASISTENCIA.name -> return Pair(Icons.Default.CheckCircle, Color(0xFF4CAF50))
        TipoNotificacion.MENSAJE.name -> return Pair(Icons.Default.Email, Color(0xFF2196F3))
    }
    
    // Fallback: analizar el título
    val titulo = notificacion.titulo
    return when {
        titulo.contains("libro", ignoreCase = true) || titulo.contains("📚") -> 
            Pair(Icons.AutoMirrored.Filled.MenuBook, Color(0xFF4CAF50))
        titulo.contains("reserva", ignoreCase = true) -> 
            Pair(Icons.Default.BookmarkAdded, Color(0xFF2196F3))
        titulo.contains("solicitud", ignoreCase = true) -> 
            Pair(Icons.AutoMirrored.Filled.Assignment, Color(0xFFFF9800))
        titulo.contains("clase", ignoreCase = true) -> 
            Pair(Icons.Default.Videocam, Color(0xFF9C27B0))
        titulo.contains("nota", ignoreCase = true) || titulo.contains("progreso", ignoreCase = true) -> 
            Pair(Icons.AutoMirrored.Filled.TrendingUp, Color(0xFFFF5722))
        else -> 
            Pair(Icons.Default.Notifications, Color(0xFF607D8B))
    }
}

fun formatearFecha(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (e: Exception) {
        "Fecha desconocida"
    }
}