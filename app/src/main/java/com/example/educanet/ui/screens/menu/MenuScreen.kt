package com.example.educanet.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.ui.common.Logo
import com.example.educanet.viewmodel.MenuViewModel

@Composable
fun MenuScreen(
    nombre: String,
    rol: String,
    onLibroClick: () -> Unit = {},
    onVideoClick: () -> Unit = {},
    onClaseVirtualClick: () -> Unit = {},
    onProgresoAcademicoClick: () -> Unit = {},
    onVerNotificaciones: () -> Unit = {},
    onCameraClick: () -> Unit = {},
    onLogout: () -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {

    val hasUnreadNotifications by menuViewModel.hasUnreadNotifications.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Logo(modifier = Modifier.size(100.dp))
        Spacer(modifier = Modifier.height(16.dp))


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Bienvenido, $nombre", fontWeight = FontWeight.Bold)
                Text("Rol: $rol", fontSize = 14.sp)
            }
            IconButton(onClick = {
                menuViewModel.checkForUnreadNotifications()
                onVerNotificaciones()
            }) {
                BadgedBox(badge = {
                    if (hasUnreadNotifications) {
                        Badge()
                    }
                }) {
                    Icon(Icons.Default.Notifications, contentDescription = "Ver Notificaciones")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Menu EducaNet",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF2694EE),
            fontSize = 30.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(24.dp))


        TextButton(onClick = onLibroClick) {
            Text("Libros y Artículos", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onVideoClick) {
            Text("Videos de Apoyo", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onClaseVirtualClick) {
            Text("Clases Virtuales", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onProgresoAcademicoClick) {
            Text("Progreso Académico", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(Modifier.height(24.dp))

        TextButton(onClick = onCameraClick) {
            Text("Cámara", color = Color(0xFF090909), fontSize = 24.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onLogout,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cerrar sesión")
        }
    }
}
