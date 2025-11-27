package com.example.educanet.ui.screens.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // Importar el contexto
import androidx.lifecycle.viewmodel.compose.viewModel // Importar para usar viewModel()
import com.example.educanet.ui.common.FotoPerfil // Importar el composable de la foto
import com.example.educanet.viewmodel.PerfilViewModel // Importar el ViewModel

@Composable
fun PerfilAlumnoScreen(
    onLogout: () -> Unit = {},
    // Inyectar el ViewModel
    perfilViewModel: PerfilViewModel = viewModel()
) {
    // 1. Obtener el estado del ViewModel
    val uiState by perfilViewModel.uiState.collectAsState()
    val context = LocalContext.current // Obtener el contexto para el ViewModel

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top, // Cambié a Top para dar espacio a la foto
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(24.dp))

        // 2. Insertar FotoPerfil
        FotoPerfil(
            fotoUrl = uiState.fotoUrl,           // URL cargada desde Firestore
            isUploading = uiState.isUploading, // Estado de carga para el spinner
            onImageSelected = { uri ->           // Callback al seleccionar una nueva Uri
                perfilViewModel.onImageSelectedAndSave(context, uri)
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Panel de Alumno",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFD32F2F)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Usar el nombre cargado desde el ViewModel
        Text(
            "Bienvenido Alumno ${uiState.nombre}",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Rol: Alumno",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Mostrar errores si los hay
        if (uiState.errorMessage != null) {
            Text(
                text = "Error: ${uiState.errorMessage}",
                color = Color.Red,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            onClick = {
                onLogout()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFD32F2F)
            )
        ) {
            Text("Cerrar Sesión")
        }
    }
}