package com.example.educanet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.educanet.ui.theme.EducaNetTheme

import android.os.Handler
import android.os.Looper
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.educanet.ui.screens.login.LoginScreen
import com.example.educanet.ui.screens.splash.SplashSplash

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
    var showLogin by rememberSaveable{ mutableStateOf(false) }

    val handler = remember { Handler(Looper.getMainLooper()) }
    LaunchedEffect(Unit) {
        handler.postDelayed({showLogin = true}, 2000L)
    }

    MaterialTheme {
        Surface {
            if (!showLogin) {
                SplashSplash()
            } else {
                LoginScreen()
            }
        }
    }
}