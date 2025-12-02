package com.example.educanet

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import com.example.educanet.navigation.AppNavegacion
import com.example.educanet.ui.screens.splash.SplashScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Cerrar cualquier sesión anterior al iniciar la app
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Log.d("DEBUG_SESION", "Cerrando sesión anterior del usuario: ${auth.currentUser?.email}")
            auth.signOut()
            Toast.makeText(this, "Sesión anterior cerrada", Toast.LENGTH_SHORT).show()
        }
        
        enableEdgeToEdge()
        setContent {
            MyApp()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Cerrar sesión cuando la app se destruye
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            Log.d("DEBUG_SESION", "App cerrada - Cerrando sesión de: ${auth.currentUser?.email}")
            auth.signOut()
        }
    }
}

@Composable
fun MyApp() {
    var showLogin by rememberSaveable { mutableStateOf(false) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    LaunchedEffect(Unit) {
        handler.postDelayed({ showLogin = true }, 2000L)
    }

    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            if (!showLogin) {
                SplashScreen()
            } else {
                AppNavegacion()
            }
        }
    }
}
