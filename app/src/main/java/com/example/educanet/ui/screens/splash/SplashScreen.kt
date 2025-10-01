package com.example.nombrecaso.ui.screens.splash

import android.window.SplashScreen
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import com.example.educanet.R

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,

    ) {
    // Animatable para escala
    val scale = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    // Lanzamos animación solo una vez al iniciar el Composable
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 500,
                easing = androidx.compose.animation.core.CubicBezierEasing(0.25f, 0.46f, 0.45f, 0.94f)
            )
        )
    }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomCenter // Anclamos la imagen al fondo centro para el transform-origin
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo),
            contentDescription = "Logo",
            modifier = Modifier
                .size(460.dp)
                // El origen del scale será el pivotY = altura (parte inferior), pivotX = mitad (centro)
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
                    alpha = 1f // opacidad constante
                }
        )
    }
}
