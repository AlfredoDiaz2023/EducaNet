package com.example.educanet.ui.screens.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.educanet.R
import com.example.educanet.repository.NotificacionRepository
import com.example.educanet.viewmodel.MenuViewModel
import kotlinx.coroutines.launch

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
    onAdminPanelClick: () -> Unit = {},
    onVerResenasClick: () -> Unit = {},
    onLogout: () -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {
    // Estado para badge de notificaciones
    var hayNotificacionesSinLeer by remember { mutableStateOf(false) }
    val notificacionRepo = remember { NotificacionRepository() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Verificar notificaciones sin leer al cargar
    LaunchedEffect(Unit) {
        scope.launch {
            hayNotificacionesSinLeer = notificacionRepo.hayNotificacionesSinLeer()
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Foto de perfil clickeable - solo navega al perfil, NO abre galería
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.clickable { onPerfilClick() }
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(if (!fotoUrl.isNullOrEmpty()) fotoUrl else R.drawable.ic_user_placeholder)
                        .crossfade(true)
                        .build(),
                    placeholder = painterResource(R.drawable.ic_user_placeholder),
                    error = painterResource(R.drawable.ic_user_placeholder),
                    contentDescription = "Foto de perfil",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentScale = ContentScale.Crop
                )

                // Icono de editar
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 4.dp
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar foto",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Bienvenido, $nombre",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Rol: $rol",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

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
                    MenuButton(
                        icon = Icons.AutoMirrored.Filled.MenuBook,
                        text = "Libros",
                        color = Color(0xFF4CAF50),
                        onClick = onLibroClick,
                        modifier = Modifier.weight(1f)
                    )
                    MenuButton(
                        icon = Icons.Default.VideoLibrary,
                        text = "Videos de Apoyo",
                        color = Color(0xFF2196F3),
                        onClick = onVideoClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 2: Clases Virtuales y Progreso (Progreso no visible para Apoderado)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuButton(
                        icon = Icons.Default.Videocam,
                        text = "Clases Virtuales",
                        color = Color(0xFF9C27B0),
                        onClick = onClaseVirtualClick,
                        modifier = Modifier.weight(1f)
                    )
                    // Progreso Académico solo visible para Alumno y Profesor
                    if (rol != "Apoderado") {
                        MenuButton(
                            icon = Icons.AutoMirrored.Filled.TrendingUp,
                            text = "Progreso Académico",
                            color = Color(0xFFFF9800),
                            onClick = onProgresoAcademicoClick,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        // Para Apoderado, mostrar botón de Ver Progreso del Hijo
                        MenuButton(
                            icon = Icons.Default.FamilyRestroom,
                            text = "Progreso de mi Hijo/a",
                            color = Color(0xFFFF9800),
                            onClick = onPerfilClick,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Fila 3: Notificaciones y Mi Perfil
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuButtonWithBadge(
                        icon = Icons.Default.Notifications,
                        text = "Notificaciones",
                        color = Color(0xFFF44336),
                        showBadge = hayNotificacionesSinLeer,
                        onClick = {
                            onVerNotificaciones()
                            hayNotificacionesSinLeer = false
                        },
                        modifier = Modifier.weight(1f)
                    )
                    MenuButton(
                        icon = Icons.Default.AccountCircle,
                        text = "Mi Perfil",
                        color = Color(0xFF607D8B),
                        onClick = onPerfilClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 4: Cámara y Ver Reseñas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MenuButton(
                        icon = Icons.Default.CameraAlt,
                        text = "Tomar Foto",
                        color = Color(0xFF00BCD4),
                        onClick = onCameraClick,
                        modifier = Modifier.weight(1f)
                    )
                    MenuButton(
                        icon = Icons.Default.RateReview,
                        text = "Ver Reseñas",
                        color = Color(0xFFE91E63),
                        onClick = onVerResenasClick,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Fila 5: Panel de Administración (solo para admin)
                if (rol == "Administrador") {
                    MenuButton(
                        icon = Icons.Default.AdminPanelSettings,
                        text = "Panel de Administración",
                        color = Color(0xFF673AB7),
                        onClick = onAdminPanelClick,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Botón de cerrar sesión
            Button(
                onClick = onLogout,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text("Cerrar Sesión", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
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