package com.example.educanet.ui.screens.progresoacademico

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import com.example.educanet.R 
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.repository.ProgresoAcademicoRepository

@Composable
fun ProgresoAcademicoScreen(
    rol: String,
    onBack: () -> Unit,
    onAddNota: () -> Unit,
) {
    val context = LocalContext.current
    val repo = remember { ProgresoAcademicoRepository() }
    var notas by remember { mutableStateOf<List<ProgresoAcademico>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val resultado = repo.obtenerNotas()
            notas = resultado.progresoAcademico
        } catch (e: Exception) {
            Toast.makeText(context, "Error al cargar notas", Toast.LENGTH_SHORT).show()
        } finally {
            cargando = false
        }
    }

    // Fondo con imagen
    Box(modifier = Modifier.fillMaxSize()) {
        // Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.logo), // Puedes usar otro drawable de tu proyecto
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.08f // Transparencia del fondo
        )

        // Contenido principal sobre el fondo
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo más pequeño y ligeramente transparente
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Logo EducaNet",
                modifier = Modifier
                    .size(80.dp)
                    .graphicsLayer(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Progreso Académico", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.height(16.dp))

            if (cargando) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(notas) { nota ->
                        NotaItem(nota)
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
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text("Agregar Nota")
                }
            }
        }
    }
}

@Composable
fun NotaItem(nota: ProgresoAcademico) {
    Card(
        Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Profesor: ${nota.profesor}")
            Text("Alumno: ${nota.alumno}")
            Text("Asignatura: ${nota.asignatura}")
            Text("Curso: ${nota.curso}")
            Text("Nota: ${nota.notas}")
        }
    }
}
