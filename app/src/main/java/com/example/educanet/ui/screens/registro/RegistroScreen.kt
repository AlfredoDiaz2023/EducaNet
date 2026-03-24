package com.example.educanet.ui.screens.registro

import androidx.compose.foundation.background
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.MenuAnchorType
import com.example.educanet.model.Usuario
import com.example.educanet.model.Cursos

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistroScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit = {},
    onRegisterSuccessWithUser: (Usuario) -> Unit = {},
    onApoderadoRegistrado: (String) -> Unit = {} // Nuevo callback para apoderado
) {
    val context = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmarClave by remember { mutableStateOf("") }
    var nombre by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val roles = listOf("Profesor", "Apoderado", "Alumno")
    var rol by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    
    // Estado para curso (solo para alumnos)
    var cursoSeleccionado by remember { mutableStateOf("") }
    var cursoExpanded by remember { mutableStateOf(false) }

    val viewModel: com.example.educanet.viewmodel.RegistroViewModel = viewModel()
    val cargando by viewModel.cargando.collectAsState()
    val registroExitoso by viewModel.registroExitoso.collectAsState()
    val errorMensaje by viewModel.errorMensaje.collectAsState()
    val usuarioRegistrado by viewModel.usuarioRegistrado.collectAsState()

    // Mostrar mensaje de error
    LaunchedEffect(errorMensaje) {
        if (errorMensaje.isNotEmpty()) {
            Toast.makeText(context, errorMensaje, Toast.LENGTH_LONG).show()
        }
    }

    // Cuando el registro es exitoso
    LaunchedEffect(registroExitoso, usuarioRegistrado) {
        if (registroExitoso && usuarioRegistrado != null) {
            val usuario = usuarioRegistrado!!
            if (usuario.rol == "Apoderado") {
                // Si es apoderado, navegar a la pantalla de selección de alumno
                Toast.makeText(
                    context,
                    "¡Registro exitoso! Ahora selecciona el alumno a vincular.",
                    Toast.LENGTH_SHORT
                ).show()
                viewModel.resetEstado()
                onApoderadoRegistrado(usuario.correo)
            } else {
                // Para otros roles, mostrar mensaje y redirigir al login
                Toast.makeText(
                    context, 
                    "¡Registro exitoso! Por favor inicia sesión con tu cuenta.", 
                    Toast.LENGTH_LONG
                ).show()
                viewModel.resetEstado()
                onBack()
            }
        }
    }

    val scrollState = rememberScrollState()

    // Fondo con degradado suave
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE3F2FD), // Azul muy claro
                        Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título y Subtítulo
            Text(
                text = "EducaNet",
                style = MaterialTheme.typography.displaySmall,
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Crea tu cuenta nueva",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )

            // Tarjeta del Formulario
            Card(
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // Campo Nombre
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1976D2)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Correo
                    OutlinedTextField(
                        value = correo,
                        onValueChange = { correo = it },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1976D2)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Contraseña
                    OutlinedTextField(
                        value = clave,
                        onValueChange = { clave = it },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1976D2)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Campo Confirmar Contraseña
                    OutlinedTextField(
                        value = confirmarClave,
                        onValueChange = { confirmarClave = it },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Color(0xFF1976D2)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Selección de Rol
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = rol,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Seleccionar Rol") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF1976D2)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF1976D2),
                                focusedLabelColor = Color(0xFF1976D2)
                            )
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
                                        // Limpiar curso si cambia de rol
                                        if (opcion != "Alumno") {
                                            cursoSeleccionado = ""
                                        }
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when(opcion) {
                                                "Profesor" -> Icons.Default.School
                                                "Alumno" -> Icons.Default.Person
                                                "Apoderado" -> Icons.Default.FamilyRestroom
                                                "Administrador" -> Icons.Default.AdminPanelSettings
                                                else -> Icons.Default.Person
                                            },
                                            contentDescription = null,
                                            tint = Color(0xFF1976D2)
                                        )
                                    }
                                )
                            }
                        }
                    }

                    // Selector de Curso (solo visible para Alumnos)
                    if (rol == "Alumno") {
                        Spacer(modifier = Modifier.height(16.dp))

                        ExposedDropdownMenuBox(
                            expanded = cursoExpanded,
                            onExpandedChange = { cursoExpanded = !cursoExpanded }
                        ) {
                            OutlinedTextField(
                                value = cursoSeleccionado.ifEmpty { "Seleccionar curso" },
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Curso") },
                                leadingIcon = { 
                                    Icon(
                                        Icons.Default.Class, 
                                        contentDescription = null, 
                                        tint = Color(0xFF4CAF50)
                                    ) 
                                },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cursoExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF4CAF50),
                                    focusedLabelColor = Color(0xFF4CAF50)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = cursoExpanded,
                                onDismissRequest = { cursoExpanded = false }
                            ) {
                                Cursos.lista.forEach { curso ->
                                    DropdownMenuItem(
                                        text = { Text(curso) },
                                        onClick = {
                                            cursoSeleccionado = curso
                                            cursoExpanded = false
                                        },
                                        leadingIcon = {
                                            Icon(
                                                Icons.Default.School,
                                                contentDescription = null,
                                                tint = Color(0xFF4CAF50)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // Botón de Registro
                    Button(
                        onClick = {
                            if (nombre.isBlank() || correo.isBlank() || clave.isBlank() || confirmarClave.isBlank() || rol.isBlank()) {
                                Toast.makeText(context, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show()
                            } else if (rol == "Alumno" && cursoSeleccionado.isBlank()) {
                                Toast.makeText(context, "Por favor, seleccione un curso", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.registroUsuario(correo, clave, confirmarClave, nombre, rol, cursoSeleccionado)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0), // Azul más oscuro
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = !cargando
                    ) {
                        if (cargando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                "REGISTRARSE",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Volver
            TextButton(onClick = onBack) {
                Text(
                    "¿Ya tienes cuenta? Inicia sesión",
                    color = Color(0xFF1976D2),
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}