package com.example.educanet.ui.screens.imagen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImagePickerScreen(onBack: () -> Unit) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }
    var uploadMessage by remember { mutableStateOf<String?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    // Launcher para abrir la galería
    val pickImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uploadMessage = null
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Seleccionar Imagen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Button(
                onClick = { pickImageLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Abrir galería")
            }

            Spacer(modifier = Modifier.height(20.dp))

            selectedImageUri?.let { uri ->
                Image(
                    painter = rememberAsyncImagePainter(uri),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para subir al Storage
                Button(
                    onClick = {
                        uploadMessage = null
                        isUploading = true
                    },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isUploading) "Subiendo..." else "Subir a Firebase Storage")
                }

                // Efecto para ejecutar la subida cuando isUploading cambia
                if (isUploading) {
                    LaunchedEffect(uri, isUploading) {
                        try {
                            val storage = FirebaseStorage.getInstance()
                            val fileName = "images/${System.currentTimeMillis()}.jpg"
                            val ref = storage.reference.child(fileName)

                            // putFile y esperar resultado
                            val taskSnapshot = ref.putFile(uri).await()

                            // Obtener URL de descarga
                            val downloadUrl = ref.downloadUrl.await().toString()
                            uploadMessage = "Imagen subida. URL: $downloadUrl"
                            snackbarHostState.showSnackbar(message = uploadMessage!!)
                        } catch (e: Exception) {
                            uploadMessage = "Error al subir: ${e.message}"
                            snackbarHostState.showSnackbar(message = uploadMessage!!)
                        } finally {
                            isUploading = false
                        }
                    }
                }
            }

            // Mensaje adicional en texto (opcional)
            uploadMessage?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(it)
            }
        }
    }
}
