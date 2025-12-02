package com.example.educanet.ui.screens.progresoacademico

import androidx.compose.foundation.layout.* // Column, Spacer, fillMaxWidth, fillMaxSize, padding, height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.* // Scaffold, Button, Text, OutlinedTextField, etc.
import androidx.compose.runtime.* // remember, LaunchedEffect, collectAsState
import androidx.compose.ui.Alignment
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier // Import necesario para usar Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp // Import necesario para usar 16.dp, 8.dp, etc.
import com.example.educanet.viewmodel.AddProgresoAcademicoViewModel

@OptIn(ExperimentalMaterial3Api::class)
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Agregar Progreso",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.profesor,
                onValueChange = {},
                readOnly = true,
                label = { Text("Profesor") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF2196F3)
                    )
                },
                trailingIcon = {
                    if (uiState.isLoadingProfesor) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2196F3),
                    unfocusedBorderColor = Color.Gray,
                    disabledBorderColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Dropdown para seleccionar alumno
            var expanded by remember { mutableStateOf(false) }
            
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = uiState.alumno,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Seleccionar Alumno") },
                    trailingIcon = {
                        if (uiState.isLoadingAlumnos) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                        } else {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        }
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF4CAF50)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF4CAF50),
                        focusedLabelColor = Color(0xFF4CAF50)
                    )
                )
                
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    if (uiState.alumnos.isEmpty() && !uiState.isLoadingAlumnos) {
                        DropdownMenuItem(
                            text = { Text("No hay alumnos registrados") },
                            onClick = { expanded = false },
                            enabled = false
                        )
                    } else {
                        uiState.alumnos.forEach { alumno ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(alumno.nombre, style = MaterialTheme.typography.bodyLarge)
                                        Text(
                                            alumno.correo, 
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color.Gray
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.onAlumnoSelected(alumno)
                                    expanded = false
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color(0xFF2196F3)
                                    )
                                }
                            )
                        }
                    }
                }
            }

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
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    contentColor = Color.White
                ),
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
