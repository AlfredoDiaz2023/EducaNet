package com.example.educanet.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.educanet.ui.screens.login.LoginScreen
import com.example.educanet.ui.screens.registro.RegistroScreen
import com.example.educanet.ui.screens.perfil.PerfilAdminScreen
import com.example.educanet.ui.screens.perfil.PerfilProfesorScreen
import com.example.educanet.ui.screens.perfil.PerfilApoderadoScreen
import com.example.educanet.ui.screens.perfil.PerfilAlumnoScreen
import com.example.educanet.ui.screens.menu.MenuScreen
import com.example.educanet.ui.screens.libro.LibroScreen
import com.example.educanet.ui.screens.notificaciones.NotificacionesScreen
import com.example.educanet.ui.screens.videoApoyo.AddVideoScreen
import com.example.educanet.ui.screens.videoApoyo.VideoApoyoScreen

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()
    

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onRegisterClick = { navController.navigate("register") },
                onLoginSuccess = { user ->
                    navController.navigate("menu/${user.nombre}/${user.rol}") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }


        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = { navController.popBackStack() }
            )
        }


        composable(
            "menu/{nombre}/{rol}",
            arguments = listOf(
                navArgument("nombre") { type = NavType.StringType },
                navArgument("rol") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Usuario"
            val rol = backStackEntry.arguments?.getString("rol") ?: "Alumno"

            MenuScreen(
                nombre = nombre,
                rol = rol,
                onLibroClick = { navController.navigate("libros") },
                onVideoClick = { navController.navigate("video_apoyo") }, // Asegúrate de tener este callback en MenuScreen
                onVerNotificaciones = { navController.navigate("notificaciones") },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                },

            )
        }


        composable("libros") { 
            LibroScreen(onBack = { navController.popBackStack() })
        }

        composable("video_apoyo") {
            VideoApoyoScreen(
                onBack = { navController.popBackStack() },
                onAddVideo = { navController.navigate("add_video") }
            )
        }

        composable("add_video") {
            AddVideoScreen(onBack = { navController.popBackStack() })
        }


        composable(
            "perfil_admin/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Administrador"
            PerfilAdminScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(
            "perfil_profesor/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Profesor"
            PerfilProfesorScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(
            "perfil_apoderado/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Apoderado"
            PerfilApoderadoScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }

        composable(
            "perfil_alumno/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) {
            val nombre = it.arguments?.getString("nombre") ?: "Alumno"
            PerfilAlumnoScreen(nombre = nombre, onLogout = {
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            })
        }
        composable("notificaciones") {
            NotificacionesScreen(onBack = { navController.popBackStack() })
        }
    }
}
