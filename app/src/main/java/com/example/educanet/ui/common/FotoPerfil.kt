package com.example.educanet.ui.common

import android.net.Uri
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.educanet.R

@Composable
fun FotoPerfil(
    fotoUrl: String?,
    isUploading: Boolean = false,
    onImageSelected: (Uri) -> Unit,
    onCameraClick: (() -> Unit)? = null // Nuevo: callback para abrir cámara
) {
    var imagenSeleccionada by remember { mutableStateOf<Uri?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Launcher para seleccionar imagen de la galería
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        Log.d("FotoPerfil", "Launcher callback recibido, uri: $uri")
        if (uri != null) {
            try {
                Log.d("FotoPerfil", "URI seleccionada de galería: $uri")
                imagenSeleccionada = uri
                onImageSelected(uri)
            } catch (e: Exception) {
                Log.e("FotoPerfil", "Error procesando URI: ${e.message}", e)
            }
        } else {
            Log.d("FotoPerfil", "Usuario canceló la selección")
        }
    }

    val modeloImagen: Any? = remember(imagenSeleccionada, fotoUrl) {
        when {
            imagenSeleccionada != null -> {
                Log.d("FotoPerfil", "Mostrando imagen local seleccionada")
                imagenSeleccionada
            }
            !fotoUrl.isNullOrEmpty() -> {
                Log.d("FotoPerfil", "Mostrando URL de Firebase: $fotoUrl")
                fotoUrl
            }
            else -> {
                Log.d("FotoPerfil", "Mostrando Placeholder")
                R.drawable.ic_user_placeholder
            }
        }
    }

    Box(contentAlignment = Alignment.Center) {
        val context = LocalContext.current

        // Foto de perfil
        Box {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(modeloImagen)
                    .crossfade(true)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .listener(
                        onStart = { Log.d("FotoPerfil", "Coil: Iniciando carga") },
                        onSuccess = { _, _ -> Log.d("FotoPerfil", "Coil: Carga exitosa") },
                        onError = { _, result -> Log.e("FotoPerfil", "Coil: Error: ${result.throwable.message}") }
                    )
                    .build(),
                placeholder = painterResource(R.drawable.ic_user_placeholder),
                error = painterResource(R.drawable.ic_user_placeholder),
                contentDescription = "Foto de perfil",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    .clickable(enabled = !isUploading) {
                        if (onCameraClick != null) {
                            showDialog = true
                        } else {
                            launcherGaleria.launch("image/*")
                        }
                    },
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

        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    // Diálogo para elegir entre cámara y galería
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
                            showDialog = false
                            onCameraClick?.invoke()
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
                            showDialog = false
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
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

// Función auxiliar para actualizar foto desde URI de cámara
@Composable
fun FotoPerfilConCamara(
    fotoUrl: String?,
    isUploading: Boolean = false,
    capturedImageUri: Uri?,
    onImageSelected: (Uri) -> Unit,
    onCameraClick: () -> Unit
) {
    var imagenSeleccionada by remember { mutableStateOf<Uri?>(null) }

    // Actualizar cuando se capture una imagen de la cámara
    LaunchedEffect(capturedImageUri) {
        capturedImageUri?.let { uri ->
            imagenSeleccionada = uri
            onImageSelected(uri)
        }
    }

    FotoPerfil(
        fotoUrl = if (imagenSeleccionada != null) imagenSeleccionada.toString() else fotoUrl,
        isUploading = isUploading,
        onImageSelected = { uri ->
            imagenSeleccionada = uri
            onImageSelected(uri)
        },
        onCameraClick = onCameraClick
    )
}