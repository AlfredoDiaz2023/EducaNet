package com.example.educanet.ui.screens.splash

import android.window.SplashScreen
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.educanet.R
import kotlinx.coroutines.delay
import kotlin.math.tan

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    // Duración total en milisegundos
    val duration = 800

    // Animables para skewX y skewY
    val skewX = remember { Animatable(0f) }
    val skewY = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Animación de skewX y skewY con valores de keyframes
        // 0% (skew 0deg, 0deg)
        skewX.snapTo(0f)
        skewY.snapTo(0f)

        // 30% (skew 25deg, 25deg)
        skewX.animateTo(25f, animationSpec = tween(durationMillis = 240, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))
        skewY.animateTo(25f, animationSpec = tween(durationMillis = 240, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))

        delay(80) // Esperar para la siguiente transición (40% en el keyframe)

        // 40% (skew -15deg, -15deg)
        skewX.animateTo(-15f, animationSpec = tween(durationMillis = 80, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))
        skewY.animateTo(-15f, animationSpec = tween(durationMillis = 80, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))

        delay(80) // Esperar para la siguiente transición (50% en el keyframe)

        // 50% (skew 15deg, 15deg)
        skewX.animateTo(15f, animationSpec = tween(durationMillis = 120, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))
        skewY.animateTo(15f, animationSpec = tween(durationMillis = 120, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))

        delay(120) // Esperar para la siguiente transición (65% en el keyframe)

        // 65% (skew -5deg, -5deg)
        skewX.animateTo(-5f, animationSpec = tween(durationMillis = 150, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))
        skewY.animateTo(-5f, animationSpec = tween(durationMillis = 150, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))

        delay(120) // Esperar para la siguiente transición (75% en el keyframe)

        // 75% (skew 5deg, 5deg)
        skewX.animateTo(5f, animationSpec = tween(durationMillis = 120, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))
        skewY.animateTo(5f, animationSpec = tween(durationMillis = 120, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))

        delay(160) // Esperar para la siguiente transición (100% en el keyframe)

        // 100% (skew 0deg, 0deg)
        skewX.animateTo(0f, animationSpec = tween(durationMillis = 160, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))
        skewY.animateTo(0f, animationSpec = tween(durationMillis = 160, easing = CubicBezierEasing(0.55f, 0.085f, 0.68f, 0.53f)))
    }

    // Convertir grados a radianes para la función de tan
    fun degToRad(deg: Float) = deg * (Math.PI / 180f).toFloat()

    // Aplicar skew usando scaleX y scaleY
    val skewXRad = tan(degToRad(skewX.value))
    val skewYRad = tan(degToRad(skewY.value))

    // Aplicamos la transformación a la imagen
    Box(
        modifier = modifier.graphicsLayer(
            transformOrigin = TransformOrigin(0.5f, 0.5f),
            scaleX = 1 + skewXRad, // Aplicamos el efecto de skew en X
            scaleY = 1 + skewYRad  // Aplicamos el efecto de skew en Y
        )
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo), // Cambia esto por tu logo
            contentDescription = "Logo animado",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
