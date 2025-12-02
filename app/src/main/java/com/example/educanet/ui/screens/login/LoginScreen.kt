package com.example.educanet.ui.screens.login

import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.educanet.model.Usuario

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.repository.AuthRepository
import com.example.educanet.ui.common.Logo
import com.example.educanet.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onRegisterClick: () -> Unit = {},
    onLoginSuccess: (Usuario: com.example.educanet.model.Usuario) -> Unit = {}

){ // Funcion de inicio de sesion
    // Variable que permite obtener en tiempo de ejecucion el estado de ciclo de vida app
    val context = LocalContext.current

    // Variable para el correo
    var correo by remember { mutableStateOf("") }

    //Variable para almacenar la clave del usuario
    var pass by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val viewModel: LoginViewModel = viewModel()

    // Variable es para almacenar el dato de usuario para el login
    val usuario by viewModel.usuario.collectAsState()
    val carga by viewModel.cargaLogin.collectAsState()
    val loginExitoso by viewModel.loginExitoso.collectAsState()
    val errorMensaje by viewModel.errorMensaje.collectAsState()

    //Variable de conexion al Auth
    val repositorio = AuthRepository()

    // Observar éxito del registro
    LaunchedEffect(loginExitoso) {
        if (loginExitoso) {
            Toast.makeText(context, "LogIn exitoso", Toast.LENGTH_SHORT).show()

        }
    }

    // Observar errores
    LaunchedEffect(errorMensaje) {
        if (errorMensaje.isNotEmpty()) {
            Toast.makeText(context, errorMensaje, Toast.LENGTH_LONG).show()
        }
    }

    // Funcion que observa cuando el usuario se loque
    LaunchedEffect(usuario) {
        usuario?.let {
            // Modifica este 'when' para incluir el nuevo rol
            val mensaje = when (it.rol) {
                "Administrador" -> "Bienvenido Admin: ${it.nombre}"
                "Profesor" -> "Bienvenido Profesor: ${it.nombre}"
                "Apoderado" -> "Bienvenido Apoderado: ${it.nombre}"
                else -> "Bienvenido Alumno: ${it.nombre}" //
            }
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            onLoginSuccess(it)

        }
    }
    
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
        // Componente Column para configurar la organizacion visual de los componentes
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Logo(modifier = Modifier.size(120.dp))
            
            Spacer(Modifier.height(24.dp))
            
            // Componente Text para agregar un texto que indique en que vista me encuentro
            Text("Iniciar Sesión",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1565C0),
                fontWeight = FontWeight.Bold
            )
            // Espacio para alejar el text del input
            Spacer(Modifier.height(32.dp))

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
                    // Componente OutlinedTextField para crear el input del usuario
                    OutlinedTextField(
                        value = correo, // Obtener el valor del input y guardarlo en la variable usuario
                        onValueChange = { correo = it}, // Actualizar la variable usuario con el nuevo ingreso del input
                        label = { Text("Correo Electrónico")}, // Agregar titulo Usuario al input
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF1976D2)) },
                        singleLine = true, // Permite que el texto del input quede en una sola linea
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(), // El input rellena la pantalla segun su ancho
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )

                    // Espacio para alejar los input
                    Spacer(Modifier.height(16.dp))

                    // Componente OutlinedTextField para crear el input de la password
                    OutlinedTextField(
                        value = pass, // Obtener el valor del input y guardarlo en la variable pass
                        onValueChange = { pass = it}, // Actualizar la variable usuario con el nuevo ingreso del input
                        label = { Text("Contraseña")}, // Agregar titulo Usuario al input
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1976D2)) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null,
                                    tint = Color.Gray
                                )
                            }
                        },
                        singleLine = true, // Permite que el texto del input quede en una sola linea
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(), // Oculta la cpntraseña al escribirla
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Define un teclado para ingresar el dato al input
                        modifier = Modifier.fillMaxWidth(), // El input rellena la pantalla segun su ancho
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF1976D2),
                            focusedLabelColor = Color(0xFF1976D2)
                        )
                    )
                    // Espacio para alejar el input del boton
                    Spacer(Modifier.height(24.dp))

                    // Componente Button para agregar un boton a la vista login
                    Button(
                        onClick = {
                            if (correo.isEmpty() || pass.isEmpty()) {
                                Toast.makeText(context, "Completar todos los campos", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.login(correo, pass)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF1565C0), // Establecer el color de Fondo
                            contentColor = Color.White // Establece el color de texto
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = !carga
                    ) {
                        if (carga){
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        }else {
                            Text("ENTRAR", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Agregar boton de registro
            TextButton(onClick = onRegisterClick){
                Text("¿No tienes cuenta? ", color = Color.Gray)
                Text("Regístrate aquí", color = Color(0xFF1565C0), fontWeight = FontWeight.Bold)
            }
        }
    }
}