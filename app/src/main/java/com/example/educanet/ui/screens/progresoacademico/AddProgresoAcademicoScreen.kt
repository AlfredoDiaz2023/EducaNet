package com.example.educanet.ui.screens.progresoacademico

import androidx.compose.foundation.layout.* // Column, Spacer, fillMaxWidth, fillMaxSize, padding, height
import androidx.compose.material3.* // Scaffold, Button, Text, OutlinedTextField, etc.
import androidx.compose.runtime.* // remember, LaunchedEffect, collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier // ✅ Import necesario para usar Modifier
import androidx.compose.ui.unit.dp // ✅ Import necesario para usar 16.dp, 8.dp, etc.
import com.example.educanet.viewmodel.AddProgresoAcademicoViewModel

@Composable
fun AddProgresoAcademicoScreen(
    onBack: () -> Unit,
    viewModel: AddProgresoAcademicoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) onBack()
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = uiState.profesor,
                onValueChange = viewModel::onProfesorChange,
                label = { Text("Profesor") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.alumno,
                onValueChange = viewModel::onAlumnoChange,
                label = { Text("Alumno") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.asignatura,
                onValueChange = viewModel::onAsignaturaChange,
                label = { Text("Asignatura") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.curso,
                onValueChange = viewModel::onCursoChange,
                label = { Text("Curso") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.notas,
                onValueChange = viewModel::onNotasChange,
                label = { Text("Nota") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.saveNota() },
                enabled = !uiState.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isSaving)
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                else
                    Text("Guardar Nota")
            }
        }
    }
}
