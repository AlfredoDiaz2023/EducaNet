package com.example.educanet.ui.screens.libro

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.educanet.viewmodel.AddLibroViewModel
import android.util.Log
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

@Composable
fun AddLibroScreen(
    onBack: () -> Unit,
    addLibroViewModel: AddLibroViewModel = viewModel()
) {
    val uiState by addLibroViewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Lanzador para seleccionar imagen (GetContent)
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            Log.d("DEBUG_URI", "onResult URI: $uri")
            addLibroViewModel.onImagenChange(uri)
        }
    )

    // Lanzador para permisos runtime
    val permisoImagenes = Manifest.permission.READ_MEDIA_IMAGES // Android 13+
    val launcherPermisos = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            Log.d("DEBUG_PERM", "Permiso concedido")
            imagePickerLauncher.launch("image/*")
        } else {
            Log.d("DEBUG_PERM", "Permiso DENEGADO")
            // se puede mostrar snackbar o pedir permiso alternativo
        }
    }

    // Si quieres también soportar dispositivos < Android 13, podrías condicionar la solicitud
    // con Build.VERSION.SDK_INT, pero RequestPermission con READ_EXTERNAL_STORAGE funciona análogo.

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onBack()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Libro", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.nombre,
                onValueChange = addLibroViewModel::onNombreChange,
                label = { Text("Nombre del Libro") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.nivel,
                onValueChange = addLibroViewModel::onNivelChange,
                label = { Text("Nivel") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.cantidad.toString(),
                onValueChange = addLibroViewModel::onCantidadChange,
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    // lanzar permiso antes de abrir galería
                    launcherPermisos.launch(permisoImagenes)
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
            ) {
                Text("Seleccionar Imagen desde Galería")
            }

            Spacer(modifier = Modifier.height(12.dp))

            uiState.imagenUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { addLibroViewModel.saveLibro(context) },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
            ) {
                if (uiState.isSaving) CircularProgressIndicator() else Text("Guardar Libro")
            }
        }
    }
}
