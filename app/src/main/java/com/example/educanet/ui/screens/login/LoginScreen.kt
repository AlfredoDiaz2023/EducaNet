package com.example.educanet.ui.screens.login

import androidx.compose.runtime.Composable
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.viewmodel.LoginViewModel

@Composable
fun LoginScreen() { // Funcion de inicio de sesion
   // Variable que permite obtener en tiempo de ejecucion el estado del ciclo de vida app
    val context = LocalContext.current

    // Variable para el correo
    var correo by remember { mutableStateOf("")}

    val viewModel: LoginViewModel = viewModel()
    // Variable es para almacenar el dato de usuario para el login
    val user by viewModel.usuario.collectAsState() // *Cambiar
    val carga by viewModel.cargaLogin.collectAsState()

    //Variable es para almacenar el dato de la password para el login
    var pass by remember { mutableStateOf("") }

    //Funcion que observa cuando el usuario se logea
    LaunchedEffect(user) {
        user?.let {
            val mensaje = when (it.rol){
                "admin" -> "Bienvenido Admin: ${it.nombre}"
                else -> "Bienvenido: ${it.nombre}"
            }
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
        }
    }
    // Componente Column para configurar la organizacion visual de los componentes
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Componente Text para agregar un texto que indique en que vista me encuentro
        Text("Iniciar Sesion",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50)
            )
        // Espacio para alejar el text del input
        Spacer(Modifier.height(16.dp))

        // Componente OutlinedTextField para crear el input del usuario
        OutlinedTextField(
            value = correo, // Obtener el valor del input y guardarlo en la varibale user
            onValueChange = { correo = it }, // Actualizar la variable user con el nuevo ingreso del input
            label = { Text("Usuario", color = Color(0xFF009688))}, // Agregar titulo Usuario al input
            singleLine = true, // Permite que el texto del input quede en una sola linea
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth() // El input rellena la pantalla segun su ancho
        )

        // Espacio para alejar el text del input
        Spacer(Modifier.height(10.dp))

        // Componente OutlinedTextField para crear el input del password
        OutlinedTextField(
            value = pass, // Obtener el valor del input y guardarlo en la varibale pass
            onValueChange = { pass = it }, // Actualizar la variable user con el nuevo ingreso del input
            label = { Text("Contraseña", color = Color(0xFF009688))}, // Agregar titulo Usuario al input
            singleLine = true, // Permite que el texto del input quede en una sola linea
            visualTransformation = PasswordVisualTransformation(), // Oculta la contraseña al escribirla
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Define un teclado para ingresar el dato al input
            modifier = Modifier.fillMaxWidth() // El input rellena la pantalla segun su ancho
        )
        // Espacio para alejar el input del boton
        Spacer(Modifier.height(14.dp))

        // Componente Button para agregar un boton a la vista login
        Button(
            onClick = {
                if (correo.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(context, "Completar todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.login(correo, pass)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81154C), // Establecer el color de Fondo
                contentColor = Color(0xFFC7F9CC))// Establecer el color de texto
        ) {
            if (carga){
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White)
            }else {
                Text("Entrar")
            }
        }
    }
}