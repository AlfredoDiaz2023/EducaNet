package com.example.educanet.ui.screens.videoApoyo

import android.content.Intent
import android.net.Uri
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.educanet.R
import com.example.educanet.model.VideoApoyo
import com.example.educanet.viewmodel.VideoApoyoViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
    var selectedNivel by remember { mutableStateOf("Todos") }
    
    // Obtener niveles únicos
    val niveles = remember(videos) {
        listOf("Todos") + videos.map { it.nivel }.distinct().sorted()
    }
    
    // Filtrar videos por nivel
    val videosFiltrados = remember(videos, selectedNivel) {
        if (selectedNivel == "Todos") videos
        else videos.filter { it.nivel == selectedNivel }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFE91E63).copy(alpha = 0.1f),
                            Color(0xFFFFFFFF),
                            Color(0xFF9C27B0).copy(alpha = 0.05f)
                        )
                    )
                )
        )

        // Logo de fondo con transparencia
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.04f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.VideoLibrary,
                                contentDescription = null,
                                tint = Color(0xFFE91E63),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Videos de Apoyo",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE91E63)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color(0xFFE91E63)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            floatingActionButton = {
                if (rol == "Profesor" || rol == "Administrador") {
                    ExtendedFloatingActionButton(
                        onClick = onAddVideo,
                        containerColor = Color(0xFFE91E63),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Agregar Video", fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Estadísticas
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.9f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        EstadisticaVideo(
                            icon = Icons.Default.VideoLibrary,
                            valor = videos.size.toString(),
                            label = "Total Videos",
                            color = Color(0xFFE91E63)
                        )
                        EstadisticaVideo(
                            icon = Icons.Default.School,
                            valor = videos.map { it.nivel }.distinct().size.toString(),
                            label = "Niveles",
                            color = Color(0xFF9C27B0)
                        )
                        EstadisticaVideo(
                            icon = Icons.Default.Timer,
                            valor = "${videos.sumOf { it.duracion }}",
                            label = "Min. Total",
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Filtros por nivel
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(niveles) { nivel ->
                        FilterChip(
                            selected = selectedNivel == nivel,
                            onClick = { selectedNivel = nivel },
                            label = { 
                                Text(
                                    nivel,
                                    fontWeight = if (selectedNivel == nivel) FontWeight.Bold else FontWeight.Normal
                                ) 
                            },
                            leadingIcon = if (selectedNivel == nivel) {
                                {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE91E63),
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (cargando) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Color(0xFFE91E63))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Cargando videos...",
                                color = Color(0xFFE91E63)
                            )
                        }
                    }
                } else if (videosFiltrados.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.VideoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay videos disponibles",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color.Gray
                            )
                            if (selectedNivel != "Todos") {
                                Text(
                                    "para el nivel: $selectedNivel",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(videosFiltrados, key = { it.id }) { video ->
                            VideoApoyoItemModerno(
                                video = video,
                                onPlayClick = { selectedVideo = video }
                            )
                        }
                    }
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
fun EstadisticaVideo(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valor: String,
    label: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            valor,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun VideoApoyoItemModerno(
    video: VideoApoyo,
    onPlayClick: () -> Unit
) {
    // Extraer ID del video para la miniatura
    val videoId = remember(video.video) {
        video.video
            .replace("https://www.youtube.com/embed/", "")
            .split("?")[0]
    }
    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/mqdefault.jpg"
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlayClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column {
            // Miniatura del video con overlay de reproducción
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                AsyncImage(
                    model = thumbnailUrl,
                    contentDescription = video.nombre,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
                
                // Overlay oscuro
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
                
                // Botón de play central
                Surface(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(64.dp),
                    shape = CircleShape,
                    color = Color(0xFFE91E63).copy(alpha = 0.9f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Reproducir",
                            modifier = Modifier.size(36.dp),
                            tint = Color.White
                        )
                    }
                }
                
                // Duración en esquina
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.8f)
                ) {
                    Text(
                        "${video.duracion} min",
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Badge de nivel
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF9C27B0)
                ) {
                    Text(
                        video.nivel,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Información del video
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    video.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profesor
                    video.profesor?.let { profesor ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFE91E63).copy(alpha = 0.15f),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        profesor.nombre.firstOrNull()?.uppercase() ?: "P",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE91E63)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                profesor.nombre,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    // Botón ver
                    FilledTonalButton(
                        onClick = onPlayClick,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFFE91E63),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ver", fontWeight = FontWeight.Bold)
                    }
                }
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
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header con gradiente
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFFE91E63),
                                    Color(0xFF9C27B0)
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                video.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                tint = Color.White
                            )
                        }
                    }
                }
                
                // Miniatura del video
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    val thumbnailUrl = "https://img.youtube.com/vi/$videoId/hqdefault.jpg"
                    
                    AsyncImage(
                        model = thumbnailUrl,
                        contentDescription = video.nombre,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                    
                    // Overlay con botón de reproducir
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(72.dp),
                            shape = CircleShape,
                            color = Color(0xFFE91E63)
                        ) {
                            IconButton(
                                onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeWatchUrl))
                                    context.startActivity(intent)
                                },
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Reproducir en YouTube",
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
                
                // Información adicional
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Botón para abrir en YouTube
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeWatchUrl))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF0000)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reproducir en YouTube", fontWeight = FontWeight.Bold)
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (video.descripcion.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Description,
                                        contentDescription = null,
                                        tint = Color(0xFFE91E63),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Descripción",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE91E63)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    video.descripcion,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    // Info en chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFF9C27B0).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.School,
                                    contentDescription = null,
                                    tint = Color(0xFF9C27B0),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    video.nivel,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFF9C27B0),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFE91E63).copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = Color(0xFFE91E63),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${video.duracion} min",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color(0xFFE91E63),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    
                    video.profesor?.let { profesor ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF2196F3).copy(alpha = 0.15f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        profesor.nombre.firstOrNull()?.uppercase() ?: "P",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2196F3)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Profesor",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    profesor.nombre,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
