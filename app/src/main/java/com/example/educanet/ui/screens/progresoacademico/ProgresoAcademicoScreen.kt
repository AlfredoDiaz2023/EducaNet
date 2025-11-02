package com.example.educanet.ui.screens.progresoacademico

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.educanet.model.ProgresoAcademico
import com.example.educanet.repository.ProgresoAcademicoRepository

@Composable
fun ProgresoAcademicoScreen(
    rol: String,
    onBack: () -> Unit,
    onAddNota: () -> Unit,
) {
    val repo = remember { ProgresoAcademicoRepository() }
    var notas by remember { mutableStateOf<List<ProgresoAcademico>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val resultado = repo.obtenerNotas()
        notas = resultado.progresoAcademico
        cargando = false
    }

    Scaffold(
        floatingActionButton = {
            if (rol == "Profesor") {
                FloatingActionButton(onClick = onAddNota) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Nota")
                }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
                Text("Progreso Académico", style = MaterialTheme.typography.titleLarge)
            }

            Spacer(Modifier.height(16.dp))

            if (cargando) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(notas) { nota ->
                        NotaItem(nota)
                    }
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
