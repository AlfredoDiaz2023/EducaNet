package com.example.educanet.ui.screens.progresoacademico

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import java.util.Locale
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.delay
import com.example.educanet.R 
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.repository.ProgresoAcademicoRepository
import com.example.educanet.viewmodel.ProgresoAcademicoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresoAcademicoScreen(
    rol: String,
    correoAlumno: String? = null,
    onBack: () -> Unit,
    onAddNota: () -> Unit,
    viewModel: ProgresoAcademicoViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current

    val notas by viewModel.progresos.collectAsState()
    val cargando by viewModel.cargando.collectAsState()

    // Decodificar el correo si viene codificado
    val correoDecodificado = remember(correoAlumno) {
        correoAlumno?.let {
            try {
                val decoded = URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
                Log.d("ProgresoScreen", "Correo decodificado: $decoded")
                decoded
            } catch (e: Exception) {
                Log.e("ProgresoScreen", "Error decodificando correo: ${e.message}")
                it
            }
        }
    }

    // Cargar datos con el correo del alumno
    LaunchedEffect(correoDecodificado, rol) {
        Log.d("ProgresoScreen", "LaunchedEffect - Correo: $correoDecodificado, Rol: $rol")
        if (!correoDecodificado.isNullOrEmpty()) {
            viewModel.cargarNotasPorCorreo(correoDecodificado, rol)
        } else {
            viewModel.refreshProgresos()
        }
    }

    // Auto-refresh cada 15 segundos
    LaunchedEffect(correoDecodificado, rol) {
        while (true) {
            delay(15000) // 15 segundos
            Log.d("ProgresoScreen", "Auto-refresh - Correo: $correoDecodificado, Rol: $rol")
            if (!correoDecodificado.isNullOrEmpty()) {
                viewModel.cargarNotasPorCorreo(correoDecodificado, rol)
            } else {
                viewModel.refreshProgresos()
            }
        }
    }

    // Calcular estadísticas
    val estadisticas = remember(notas) {
        if (notas.isEmpty()) {
            EstadisticasAcademicas()
        } else {
            EstadisticasAcademicas(
                promedio = notas.map { it.notas }.average(),
                notaMaxima = notas.maxOfOrNull { it.notas } ?: 0.0,
                notaMinima = notas.minOfOrNull { it.notas } ?: 0.0,
                totalAsignaturas = notas.map { it.asignatura }.distinct().size,
                notasPorAsignatura = notas.groupBy { it.asignatura }
                    .mapValues { (_, list) -> list.map { it.notas }.average() }
            )
        }
    }

    // Fondo con imagen
    Box(modifier = Modifier.fillMaxSize()) {
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
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text(
                    "Progreso Académico",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(16.dp))

            if (cargando) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                // Mostrar estadísticas para alumnos
                if (rol == "Alumno" && notas.isNotEmpty()) {
                    EstadisticasCard(estadisticas)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        "Progreso por Asignatura",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Barras de progreso por asignatura
                    ProgresoAsignaturasCard(estadisticas.notasPorAsignatura)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Lista de notas detalladas
                Text(
                    "Detalle de Notas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (notas.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No hay notas registradas",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(notas) { nota ->
                            NotaItemMejorado(nota)
                        }
                    }
                }
            }

            if (rol == "Profesor") {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onAddNota,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar Nota", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

data class EstadisticasAcademicas(
    val promedio: Double = 0.0,
    val notaMaxima: Double = 0.0,
    val notaMinima: Double = 0.0,
    val totalAsignaturas: Int = 0,
    val notasPorAsignatura: Map<String, Double> = emptyMap()
)

@Composable
fun EstadisticasCard(estadisticas: EstadisticasAcademicas) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "📊 Resumen de Rendimiento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EstadisticaItem(
                    icon = Icons.Default.Timeline,
                    label = "Promedio",
                    value = String.format(Locale.US, "%.1f", estadisticas.promedio),
                    color = getColorForNota(estadisticas.promedio)
                )
                EstadisticaItem(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = "Máxima",
                    value = String.format(Locale.US, "%.1f", estadisticas.notaMaxima),
                    color = Color(0xFF4CAF50)
                )
                EstadisticaItem(
                    icon = Icons.AutoMirrored.Filled.TrendingDown,
                    label = "Mínima",
                    value = String.format(Locale.US, "%.1f", estadisticas.notaMinima),
                    color = Color(0xFFF44336)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Barra de progreso general
            Text(
                "Progreso General",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            
            val progresoAnimado by animateFloatAsState(
                targetValue = (estadisticas.promedio / 7.0).toFloat().coerceIn(0f, 1f),
                animationSpec = tween(durationMillis = 1000),
                label = "progreso"
            )
            
            LinearProgressIndicator(
                progress = { progresoAnimado },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(RoundedCornerShape(6.dp)),
                color = getColorForNota(estadisticas.promedio),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${(estadisticas.promedio / 7.0 * 100).toInt()}% del máximo (7.0)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun EstadisticaItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            value,
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
fun ProgresoAsignaturasCard(notasPorAsignatura: Map<String, Double>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            notasPorAsignatura.forEach { (asignatura, promedio) ->
                AsignaturaProgressItem(asignatura, promedio)
                if (asignatura != notasPorAsignatura.keys.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
fun AsignaturaProgressItem(asignatura: String, promedio: Double) {
    val progresoAnimado by animateFloatAsState(
        targetValue = (promedio / 7.0).toFloat().coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "progreso_$asignatura"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Book,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = getColorForNota(promedio)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    asignatura,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                String.format(Locale.US, "%.1f", promedio),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = getColorForNota(promedio)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progresoAnimado },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = getColorForNota(promedio),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun NotaItemMejorado(nota: ProgresoAcademico) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Indicador de nota con color
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = getColorForNota(nota.notas).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp)
                ) {}
                Text(
                    String.format(Locale.US, "%.1f", nota.notas),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = getColorForNota(nota.notas)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    nota.asignatura,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        nota.profesor,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Class,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        nota.curso,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Indicador de estado
            Icon(
                imageVector = if (nota.notas >= 4.0) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (nota.notas >= 4.0) Color(0xFF4CAF50) else Color(0xFFF44336),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

fun getColorForNota(nota: Double): Color {
    return when {
        nota >= 6.0 -> Color(0xFF4CAF50)  // Verde - Excelente
        nota >= 5.0 -> Color(0xFF8BC34A)  // Verde claro - Muy bueno
        nota >= 4.0 -> Color(0xFFFF9800)  // Naranja - Aprobado
        nota >= 3.0 -> Color(0xFFFF5722)  // Naranja oscuro - Regular
        else -> Color(0xFFF44336)          // Rojo - Reprobado
    }
}
