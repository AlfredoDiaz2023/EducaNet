package com.example.educanet.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.educanet.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    onGestionLibros: () -> Unit,
    onGestionUsuarios: () -> Unit,
    onGestionAsignaturas: () -> Unit,
    onHistorialReservas: () -> Unit,
    onEditarPerfil: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel de Administración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Opciones de Administración",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Gestión de Libros
                AdminOptionCard(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    title = "Gestión de Libros",
                    description = "Crear, editar y eliminar libros del catálogo",
                    color = Color(0xFF4CAF50),
                    onClick = onGestionLibros
                )

                // Gestión de Usuarios
                AdminOptionCard(
                    icon = Icons.Default.People,
                    title = "Gestión de Usuarios",
                    description = "Administrar profesores, apoderados y alumnos",
                    color = Color(0xFF2196F3),
                    onClick = onGestionUsuarios
                )

                // Gestión de Asignaturas
                AdminOptionCard(
                    icon = Icons.Default.School,
                    title = "Gestión de Asignaturas",
                    description = "Crear, editar y eliminar asignaturas del currículo",
                    color = Color(0xFF9C27B0),
                    onClick = onGestionAsignaturas
                )

                // Historial de Reservas
                AdminOptionCard(
                    icon = Icons.Default.History,
                    title = "Historial de Reservas",
                    description = "Ver todas las reservas de libros realizadas",
                    color = Color(0xFFFF9800),
                    onClick = onHistorialReservas
                )

                // Editar Perfil
                AdminOptionCard(
                    icon = Icons.Default.Edit,
                    title = "Editar Mi Perfil",
                    description = "Modificar nombre de usuario y datos personales",
                    color = Color(0xFFFF5722),
                    onClick = onEditarPerfil
                )
            }
        }
    }
}

@Composable
fun AdminOptionCard(
    icon: ImageVector,
    title: String,
    description: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                color = color.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = color
            )
        }
    }
}
