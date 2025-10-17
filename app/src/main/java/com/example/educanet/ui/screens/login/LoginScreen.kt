package com.example.educanet.ui.screens.login

import androidx.compose.runtime.Composable

import android.widget.Toast //Mensaje emergentes
import androidx.compose.foundation.layout.* //Organizar los elementos en una vista
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardOptions //Mostrar la entre de datos al usuario
import androidx.compose.material3.* //Elementos para diseñar UI
import androidx.compose.runtime.* // Manejar los estados de la app
import androidx.compose.ui.Alignment // Alinear los elementos
import androidx.compose.ui.Modifier //Modificar el diseño visual de los elemento
import androidx.compose.ui.platform.LocalContext //obtener el contexto o estado en ejecución del ciclo de vida de la app y poder mostrar mensaje
import androidx.compose.ui.text.input.KeyboardType //Controlar el tipo de entrada para el usuario
import androidx.compose.ui.text.input.PasswordVisualTransformation //Ocultar la contraseña al escribirla
import androidx.compose.ui.unit.dp //Controlar el tamaño de los elementos
import androidx.compose.ui.graphics.Color //Controlar el color de los elementos

import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.educanet.repository.AuthRepository
import com.example.educanet.viewmodel.LoginViewModel

@Composable
fun LoginScreen(onRegisterClick: () -> Unit = {}, //Agregar al nuevo LoginScreen
                onLoginSuccess: (user: com.example.educanet.model.Usuario ) -> Unit = {}) {
    //Variable para obtener en tiempo de ejecución el estado del ciclo de vida de app
    val context = LocalContext.current

    //Variable para almacenar en nombre del usuario ** cabiar correo
    var correo by remember { mutableStateOf("") }

    //Variable para almacenar la clave del usuario
    var pass by remember { mutableStateOf("") }

    val viewModel: LoginViewModel =viewModel()
    val user by viewModel.user.collectAsState()
    val carga by viewModel.carga.collectAsState()

    val repositorio = AuthRepository()//Agregar al nuevo LoginScreen

    //Observar cuando el usuario este logueado
    LaunchedEffect(user) {
        user?.let {
            val mensaje = when (it.rol) {
                "admin" -> "Bienvenido Admin: ${it.nombre}"
                else -> "Bienvenido: ${it.nombre}"
            }
            Toast.makeText(context, mensaje, Toast.LENGTH_LONG).show()
            onLoginSuccess(it)
        }
    }

    //Configuración para organizar los elementos de la pantalla usando el componente Column()
    Column (
        modifier = Modifier
            .fillMaxSize() //Rellenar todo el espacio diponible de la pantalla
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        //Componente tipo Text() para agregar un título
        Text("Inciar Sesión",
            style = MaterialTheme.typography.headlineSmall,
            color = Color(0xFF4CAF50))

        //Componente Spacer() para agregar un separador entre los elementos
        Spacer(Modifier.height(46.dp))

        //Componente tipo OutlinedTextField() para ingresar datos por usuario
        OutlinedTextField(
            //Variable para el nombre del usuario
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo", color = Color(0xFFFF5722))},
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        //Componente Spacer() para agregar un separador entre los elementos
        Spacer(Modifier.height(10.dp))

        //Componente tipo OutlinedTextField() para ingresar datos por usuario
        OutlinedTextField(
            //Variable para la clave del usuario
            value = pass,
            onValueChange = { pass = it },
            label = { Text("Clave", color = Color(0xFFFF5722))},
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth()
        )

        //Componente Spacer() para agregar un separador entre los elementos
        Spacer(Modifier.height(30.dp))

        //Componente Button() para agrega un boton
        Button(
            onClick = {
                if (correo.isEmpty() || pass.isEmpty()) {
                    Toast.makeText(context, "Complete todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                viewModel.login(correo, pass)
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF81154C),
                contentColor = Color(0xFFC7F9CC)
            ),
            enabled = !carga
        ){
            if(carga) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.Gray)
            } else {
                Text("Entrar")
            }
        }
        //Botón para agregar registro
        Spacer(Modifier.height(16.dp))
        TextButton(onClick = onRegisterClick) {
            Text("¿No tienes cuenta? Registrate aquí", color = Color(0xFF81154C))
        }
    }
}