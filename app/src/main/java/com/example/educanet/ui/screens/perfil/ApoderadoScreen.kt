package com.example.educanet.ui.screens.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color

@Composable
fun PerfilApoderadoScreen(
    nombre: String = "Apoderado",
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Panel de Alumno",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFD32F2F)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Bienvenido Alumno $nombre",  // Muestra el nombre del alumno
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "Rol: Alumno",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(24.dp))

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