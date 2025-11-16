package com.example.educanet.ui.screens.menu

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.ui.common.FotoPerfil
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
    onCarritoClick: () -> Unit = {},
    onLogout: () -> Unit,
    menuViewModel: MenuViewModel = viewModel()
) {
    val uiState by menuViewModel.uiState.collectAsState()
    val usuario = uiState.usuario

    // Fondo con imagen
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.logo), // puedes usar otro drawable
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.08f // transparencia del fondo
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo más pequeño y transparente
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo EducaNet",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(alpha = 0.8f) // transparencia
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FotoPerfil(
                        fotoUrl = usuario?.fotoUrl,
                        isUploading = uiState.isUploadingPhoto,
                        onImageSelected = { uri ->
                            menuViewModel.updateProfilePicture(uri.toString())
                        }
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text("Bienvenido, ${usuario?.nombre ?: nombre}", fontWeight = FontWeight.Bold)
                        Text("Rol: ${usuario?.rol ?: rol}", fontSize = 14.sp)
                    }
                }

                IconButton(onClick = {
                    menuViewModel.checkForUnreadNotifications()
                    onVerNotificaciones()
                }) {
                    BadgedBox(badge = {
                        if (uiState.hasUnreadNotifications) Badge()
                    }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Ver Notificaciones")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Menu EducaNet",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2694EE),
                fontSize = 30.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(Modifier.height(24.dp))

            // Botones
            Button(
                onClick = onLibroClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text("Libros y Artículos", color = Color(0xFF090909), fontSize = 24.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onVideoClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp, // grosor del borde
                        color = Color.Black, // color del borde
                        shape = RoundedCornerShape(50) // misma forma que el botón
                    )
            ) {
                Text("Videos de Apoyo", color = Color(0xFF090909), fontSize = 24.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onClaseVirtualClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text("Clases Virtuales", color = Color(0xFF090909), fontSize = 24.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onProgresoAcademicoClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text("Progreso Académico", color = Color(0xFF090909), fontSize = 24.sp)
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onCameraClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFFFFF),
                    contentColor = Color.Black
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 2.dp,
                        color = Color.Black,
                        shape = RoundedCornerShape(50)
                    )
            ) {
                Text("Cámara", fontSize = 24.sp)
            }

            Spacer(modifier = Modifier.height(50.dp))

            Button(
                onClick = onLogout,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEE1B0B),
                    contentColor = Color.White
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Cerrar sesión")
            }
        }
    }
}
