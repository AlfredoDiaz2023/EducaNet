package com.example.educanet.ui.screens.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.ui.common.FotoPerfil
import com.example.educanet.viewmodel.MenuViewModel

@Composable
fun MenuScreen(
    nombre: String,
    rol: String,
    fotoUrl: String?, // <--- NUEVO PARÁMETRO
    onLibroClick: () -> Unit,
    onVideoClick: () -> Unit,
    onClaseVirtualClick: () -> Unit,
    onProgresoAcademicoClick: () -> Unit,
    onVerNotificaciones: () -> Unit,
    onCameraClick: () -> Unit,
    onLogout: () -> Unit,
    // ViewModel opcional si necesitas recargar datos
    menuViewModel: MenuViewModel = viewModel()
) {
    // Si quisieras usar el estado del ViewModel:
    // val uiState by menuViewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // 1. COMPONENTE FOTO DE PERFIL
        FotoPerfil(
            fotoUrl = fotoUrl,
            isUploading = false, // En el menú no subimos foto, solo mostramos
            onImageSelected = { /* Si quieres navegar al perfil al hacer click: onNavigate("perfil_...") */ }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Bienvenido, $nombre", style = MaterialTheme.typography.headlineSmall)
        Text(text = "Rol: $rol", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(30.dp))

        // BOTONES DEL MENÚ (Lógica existente)
        Button(onClick = onLibroClick, modifier = Modifier.fillMaxWidth()) {
            Text("Libros")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onVideoClick, modifier = Modifier.fillMaxWidth()) {
            Text("Videos de Apoyo")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onClaseVirtualClick, modifier = Modifier.fillMaxWidth()) {
            Text("Clases Virtuales")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onProgresoAcademicoClick, modifier = Modifier.fillMaxWidth()) {
            Text("Progreso Académico")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onVerNotificaciones, modifier = Modifier.fillMaxWidth()) {
            Text("Notificaciones")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onCameraClick, modifier = Modifier.fillMaxWidth()) {
            Text("Cámara")
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cerrar Sesión")
        }
    }
}