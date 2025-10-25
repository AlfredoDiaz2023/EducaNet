package com.example.educanet.ui.screens.registro

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }

    // Campo Rol con menú desplegable
    val roles = listOf("Profesor", "Apoderado", "Alumno")
    var rol by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val viewModel: com.example.educanet.viewmodel.RegistroViewModel = viewModel()
    val cargando by viewModel.cargando.collectAsState()
    val registroExitoso by viewModel.registroExitoso.collectAsState()
    val errorMensaje by viewModel.errorMensaje.collectAsState()

    // Observar éxito del registro
    LaunchedEffect(registroExitoso) {
        if (registroExitoso) {
            Toast.makeText(context, "Registro exitoso", Toast.LENGTH_SHORT).show()
            onRegisterSuccess()
        }
    }

    // Observar errores
    LaunchedEffect(errorMensaje) {
        if (errorMensaje.isNotEmpty()) {
            Toast.makeText(context, errorMensaje, Toast.LENGTH_LONG).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Volver")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                "Registrarse",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFF4CAF50)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Campos del formulario
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre completo *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = clave,
            onValueChange = { clave = it },
            label = { Text("Contraseña *") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = confirmarClave,
            onValueChange = { confirmarClave = it },
            label = { Text("Confirmar contraseña *") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Menú desplegable de Roles (sin Administrador)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = rol,
                onValueChange = {},
                readOnly = true,
                label = { Text("Rol *") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                roles.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            rol = opcion
                            expanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.registroUsuario(correo, clave, confirmarClave, nombre, rol)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2196F3),
                contentColor = Color.White
            ),
            enabled = !cargando
        ) {
            if (cargando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
            } else {
                Text("Registrarse", style = MaterialTheme.typography.bodyLarge)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "* Campos obligatorios",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}
