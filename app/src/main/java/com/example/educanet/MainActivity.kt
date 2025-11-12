package com.example.educanet

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.educanet.navigation.AppNavegacion
import com.example.educanet.ui.screens.splash.SplashScreen
import android.os.Handler
import android.os.Looper
import androidx.compose.runtime.saveable.rememberSaveable

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApp()
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
                // Aquí podrías mostrar el selector de imagen si quieres
                // ImagePickerExample()
            }
        }
    }
}
