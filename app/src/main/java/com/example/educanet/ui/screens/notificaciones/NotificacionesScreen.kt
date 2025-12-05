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
import com.example.educanet.R
import com.example.educanet.model.Notificacion
import com.example.educanet.repository.NotificacionRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificacionesScreen(onBack: () -> Unit) {
    val repo = remember { NotificacionRepository() }
    val scope = rememberCoroutineScope()
    var notificaciones by remember { mutableStateOf<List<Notificacion>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }
    
    // Obtener usuario actual y su rol
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""
    var esAdmin by remember { mutableStateOf(false) }
    
    // Determinar si el usuario es admin
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                val userDoc = FirebaseFirestore.getInstance()
                    .collection("usuario")
                    .document(currentUserId)
                    .get()
                    .await()
                val rol = userDoc.getString("rol") ?: ""
                esAdmin = rol.equals("admin", ignoreCase = true) || rol.equals("administrador", ignoreCase = true)
            } catch (e: Exception) {
                esAdmin = false
            }
        }
    }

    // Cargar notificaciones según el tipo de usuario
    LaunchedEffect(currentUserId, esAdmin) {
        if (currentUserId.isNotEmpty()) {
            scope.launch {
                notificaciones = if (esAdmin) {
                    repo.obtenerNotificacionesParaAdmin()
                } else {
                    repo.obtenerNotificacionesPorUsuario(currentUserId)
                }
                repo.marcarTodasComoLeidas()
                cargando = false
            }
        }
    }

    // Auto-refresh cada 15 segundos
    LaunchedEffect(currentUserId, esAdmin) {
        while (true) {
            delay(15000) // 15 segundos
            if (currentUserId.isNotEmpty()) {
                notificaciones = if (esAdmin) {
                    repo.obtenerNotificacionesParaAdmin()
                } else {
                    repo.obtenerNotificacionesPorUsuario(currentUserId)
                }
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
                                }
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
    onDelete: () -> Unit
) {
    val iconoYColor = getIconoNotificacion(notificacion.titulo)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
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
                Text(
                    notificacion.titulo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    notificacion.mensaje,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
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
    }
}

fun getIconoNotificacion(titulo: String): Pair<androidx.compose.ui.graphics.vector.ImageVector, Color> {
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