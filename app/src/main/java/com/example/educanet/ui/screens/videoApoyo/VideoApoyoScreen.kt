package com.example.educanet.ui.screens.videoApoyo

import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.R
import com.example.educanet.model.VideoApoyo
import com.example.educanet.viewmodel.VideoApoyoViewModel

@Composable
fun VideoApoyoScreen(
    rol: String,
    onBack: () -> Unit,
    onAddVideo: () -> Unit,
    videoApoyoViewModel: VideoApoyoViewModel = viewModel()
) {
    val videos by videoApoyoViewModel.videos.collectAsState()
    val cargando by videoApoyoViewModel.cargando.collectAsState()
    var selectedVideo by remember { mutableStateOf<VideoApoyo?>(null) }

    // 🔹 Fondo con imagen + contenido principal
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.logo), // tu imagen de fondo
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.08f // transparencia del fondo
        )

        // Contenido principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo superior pequeño y con transparencia
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo EducaNet",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Spacer(modifier = Modifier.width(20.dp))
                Text(
                    "Videos de Apoyo",
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (cargando) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(videos) { video ->
                        VideoApoyoItem(
                            video = video,
                            onPlayClick = { selectedVideo = video }
                        )
                    }
                }
            }

            if (rol == "Profesor") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddVideo,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Agregar Video")
                }
            }
        }
    }

    // Diálogo para reproducir el video
    selectedVideo?.let { video ->
        VideoPlayerDialog(
            video = video,
            onDismiss = { selectedVideo = null }
        )
    }
}

@Composable
fun VideoApoyoItem(
    video: VideoApoyo,
    onPlayClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onPlayClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Ícono de reproducción
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE91E63).copy(alpha = 0.15f),
                modifier = Modifier.size(60.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.PlayCircle,
                        contentDescription = "Reproducir",
                        modifier = Modifier.size(36.dp),
                        tint = Color(0xFFE91E63)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    video.nombre, 
                    style = MaterialTheme.typography.titleMedium, 
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Nivel: ${video.nivel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                video.profesor?.let {
                    Text(
                        "Profesor: ${it.nombre}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    "Duración: ${video.duracion} min",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFE91E63)
                )
            }
            
            // Botón de reproducir
            FilledTonalButton(
                onClick = onPlayClick,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = Color(0xFFE91E63).copy(alpha = 0.15f),
                    contentColor = Color(0xFFE91E63)
                )
            ) {
                Text("Ver", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VideoPlayerDialog(
    video: VideoApoyo,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    // Extraer el ID del video de YouTube de la URL embed
    val videoId = remember(video.video) {
        video.video
            .replace("https://www.youtube.com/embed/", "")
            .split("?")[0]
    }
    
    // URL para ver en YouTube
    val youtubeWatchUrl = "https://www.youtube.com/watch?v=$videoId"
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header con título y botón cerrar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        video.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                // Reproductor de YouTube usando WebView
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    YouTubePlayer(videoId = videoId)
                }
                
                // Información adicional
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Botón para abrir en YouTube
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeWatchUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFFE91E63)
                        )
                    ) {
                        Icon(
                            Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Abrir en YouTube", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (video.descripcion.isNotEmpty()) {
                        Text(
                            "Descripción:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            video.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Nivel: ${video.nivel}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            "Duración: ${video.duracion} min",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE91E63)
                        )
                    }
                    
                    video.profesor?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Profesor: ${it.nombre}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun YouTubePlayer(videoId: String) {
    val embedUrl = "https://www.youtube.com/embed/$videoId"
    
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    mediaPlaybackRequiresUserGesture = false
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                    cacheMode = WebSettings.LOAD_DEFAULT
                }
                
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        return false
                    }
                }
                webChromeClient = WebChromeClient()
                
                // Cargar directamente la URL de embed
                loadUrl(embedUrl)
            }
        },
        update = { webView ->
            webView.loadUrl(embedUrl)
        },
        modifier = Modifier.fillMaxSize()
    )
}
