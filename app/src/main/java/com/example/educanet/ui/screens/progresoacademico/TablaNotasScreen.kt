package com.example.educanet.ui.screens.progresoacademico

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.educanet.R
import com.example.educanet.model.SistemaNotas

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TablaNotasScreen(
    onBack: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.06f
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.TableChart,
                                contentDescription = null,
                                tint = Color(0xFF9C27B0),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Tabla de Notas",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF9C27B0)
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color(0xFF9C27B0)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Información del sistema
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8EAF6)),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF3F51B5)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Sistema de Calificación Chileno",
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF3F51B5)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            InfoRow("Escala de notas:", "1.0 a 7.0")
                            InfoRow("Nota mínima aprobatoria:", "4.0")
                            InfoRow("Exigencia:", "60%")
                        }
                    }
                }

                // Título de la tabla
                item {
                    Text(
                        "📊 Tabla de Conversión Porcentaje → Nota",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                }

                // Encabezado de la tabla
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Porcentaje",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Nota",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Estado",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                // Filas de la tabla
                items(SistemaNotas.tablaConversion) { rango ->
                    val colorFondo = when {
                        rango.nota >= 6.0 -> Color(0xFFE8F5E9)
                        rango.nota >= 5.0 -> Color(0xFFF1F8E9)
                        rango.nota >= 4.0 -> Color(0xFFFFF8E1)
                        rango.nota >= 3.0 -> Color(0xFFFFF3E0)
                        else -> Color(0xFFFFEBEE)
                    }
                    
                    val colorTexto = when {
                        rango.nota >= 6.0 -> Color(0xFF2E7D32)
                        rango.nota >= 5.0 -> Color(0xFF558B2F)
                        rango.nota >= 4.0 -> Color(0xFFF57F17)
                        rango.nota >= 3.0 -> Color(0xFFE65100)
                        else -> Color(0xFFC62828)
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colorFondo),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${rango.porcentajeMin}% - ${rango.porcentajeMax}%",
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                String.format("%.1f", rango.nota),
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colorTexto
                            )
                            Text(
                                rango.descripcion,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                color = colorTexto,
                                fontWeight = FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // Leyenda
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "📌 Leyenda de Colores",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            LeyendaItem(Color(0xFF4CAF50), "Excelente (6.0 - 7.0)")
                            LeyendaItem(Color(0xFF8BC34A), "Muy Bueno (5.0 - 5.9)")
                            LeyendaItem(Color(0xFFFF9800), "Suficiente (4.0 - 4.9)")
                            LeyendaItem(Color(0xFFFF5722), "Insuficiente (3.0 - 3.9)")
                            LeyendaItem(Color(0xFFF44336), "Deficiente (1.0 - 2.9)")
                        }
                    }
                }

                // Calculadora rápida
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    CalculadoraNotaCard()
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text(value, fontWeight = FontWeight.Bold, color = Color(0xFF3F51B5))
    }
}

@Composable
private fun LeyendaItem(color: Color, texto: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, RoundedCornerShape(4.dp))
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(texto, fontSize = 14.sp)
    }
}

@Composable
private fun CalculadoraNotaCard() {
    var puntajeObtenido by remember { mutableStateOf("") }
    var puntajeTotal by remember { mutableStateOf("") }
    var notaCalculada by remember { mutableStateOf<Double?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCE4EC)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Calculate,
                    contentDescription = null,
                    tint = Color(0xFFE91E63)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "🧮 Calculadora Rápida",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = puntajeObtenido,
                    onValueChange = { 
                        puntajeObtenido = it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Puntaje Obtenido") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63),
                        focusedLabelColor = Color(0xFFE91E63)
                    )
                )
                OutlinedTextField(
                    value = puntajeTotal,
                    onValueChange = { 
                        puntajeTotal = it.filter { c -> c.isDigit() || c == '.' }
                    },
                    label = { Text("Puntaje Total") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFFE91E63),
                        focusedLabelColor = Color(0xFFE91E63)
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val obtenido = puntajeObtenido.toDoubleOrNull() ?: 0.0
                    val total = puntajeTotal.toDoubleOrNull() ?: 0.0
                    if (total > 0) {
                        notaCalculada = SistemaNotas.calcularNota(obtenido, total)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Calcular Nota", fontWeight = FontWeight.Bold)
            }

            // Resultado
            notaCalculada?.let { nota ->
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(SistemaNotas.obtenerColorHex(nota))
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Nota Calculada",
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            String.format("%.1f", nota),
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp
                        )
                        Text(
                            SistemaNotas.obtenerDescripcion(nota),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        val porcentaje = puntajeObtenido.toDoubleOrNull()?.let { obt ->
                            puntajeTotal.toDoubleOrNull()?.let { tot ->
                                if (tot > 0) ((obt / tot) * 100).toInt() else 0
                            }
                        } ?: 0
                        Text(
                            "($porcentaje%)",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            if (SistemaNotas.esAprobado(nota)) "✅ APROBADO" else "❌ REPROBADO",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
