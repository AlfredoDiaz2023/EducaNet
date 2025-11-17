package com.example.educanet.ui.common

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.educanet.R

@Composable
fun FotoPerfil(
    fotoUrl: String?,                // URL actual del usuario
    isUploading: Boolean,           // Estado de carga (spinner)
    onImageSelected: (Uri) -> Unit  // Devuelve la Uri al seleccionar en galería
) {
    var imagenSeleccionada by remember { mutableStateOf<Uri?>(null) }

    // Lanzador de la galería
    val launcherGaleria = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            imagenSeleccionada = uri
            onImageSelected(uri)     // Avisamos al ViewModel
        }
    }

    Box(contentAlignment = androidx.compose.ui.Alignment.Center) {

        val imageRequest = ImageRequest.Builder(LocalContext.current)
            .data(
                imagenSeleccionada   // Foto recién elegida
                    ?: fotoUrl       // Foto desde Firestore
                    ?: R.drawable.logo // Imagen por defecto
            )
            .memoryCachePolicy(CachePolicy.DISABLED)
            .diskCachePolicy(CachePolicy.DISABLED)
            .crossfade(true)
            .build()

        AsyncImage(
            model = imageRequest,
            contentDescription = "Foto de perfil",
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Black, CircleShape)
                .clickable(enabled = !isUploading) {
                    launcherGaleria.launch("image/*")
                },
            contentScale = ContentScale.Crop
        )

        if (isUploading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    }
}