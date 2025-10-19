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
import com.example.educanet.model.Usuario

@Composable
fun LoginScreen(){ // Funcion de inicio de sesion
    // Variable que permite obtener en tiempo de ejecucion el estado de ciclo de vida app
    val context = LocalContext.current

    // Variable es para almacenar el dato de usuario para el login
    var usuario by remember { mutableStateOf("") }

    // Variable es para alamacenar el dato de la password para el login
    var pass by remember { mutableStateOf("") }

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
            color = Color(0xFF59A45B)
            )
        // Espacio para alejar el text del input
        Spacer(Modifier.height(16.dp))

        // Componente OutlinedTextField para crear el input del usuario
        OutlinedTextField(
            value = usuario, // Obtener el valor del input y guardarlo en la variable usuario
            onValueChange = { usuario = it}, // Actualizar la variable usuario con el nuevo ingreso del input
            label = { Text("Usuario", color = Color(0xFF21C2B2))}, // Agregar titulo Usuario al input
            singleLine = true, // Permite que el texto del input quede en una sola linea
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.fillMaxWidth() // El input rellena la pantalla segun su ancho

        )

        // Espacio para alejar los input
        Spacer(Modifier.height(10.dp))

        // Componente OutlinedTextField para crear el input de la password
        OutlinedTextField(
            value = pass, // Obtener el valor del input y guardarlo en la variable pass
            onValueChange = { pass = it}, // Actualizar la variable usuario con el nuevo ingreso del input
            label = { Text("Contraseña", color = Color(0xFF21C2B2))}, // Agregar titulo Usuario al input
            singleLine = true, // Permite que el texto del input quede en una sola linea
            visualTransformation = PasswordVisualTransformation(), // Oculta la cpntraseña al escribirla
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password), // Define un teclado para ingresar el dato al input
            modifier = Modifier.fillMaxWidth() // El input rellena la pantalla segun su ancho
        )
        // Espacio para alejar el input del boton
        Spacer(Modifier.height(14.dp))

        // Componente Button para agregar un boton a la vista login
        Button(
            onClick = {
                Toast.makeText(context, "Bienvenido $usuario", Toast.LENGTH_SHORT)
                    .show()
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFA40E31), // Establecer el color de Fondo
                contentColor = Color(0xFFD7EA1E) // Establece el color de texto
            )
        ) {
            Text("Entrar")
        }
    }
}