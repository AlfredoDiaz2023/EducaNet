package com.example.educanet.navigation

import androidx.compose.runtime.Composable
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
import com.google.firebase.auth.FirebaseAuth

@Composable
fun AppNavegacion() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onRegisterClick = {
                    navController.navigate("register")
                },
                onLoginSuccess = { user ->
                    // Navegar según el rol pasando el nombre como parámetro
                    when (user.rol) {
                        "admin" -> navController.navigate("perfil_admin/${user.nombre}")
                        "profesor" -> navController.navigate("perfil_profesor/${user.nombre}")
                        "apoderado" -> navController.navigate("perfil_apoderado/${user.nombre}")
                        "alumno" -> navController.navigate("perfil_alumno/${user.nombre}")


                    }
                }
            )
        }

        composable("register") {
            RegistroScreen(
                onBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            "perfil_admin/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Administrador"
            PerfilAdminScreen(
                nombre = nombre,
                onLogout = {
                    // Volver al login limpiando el back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true}
                    }
                }
            )
        }

        composable(
            "perfil_profesor/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Profesor"
            PerfilProfesorScreen(
                nombre = nombre,
                onLogout = {

                    // Volver al login limpiando el back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            "perfil_apoderado/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Apoderado"
            PerfilApoderadoScreen(
                nombre = nombre,
                onLogout = {

                    // Volver al login limpiando el back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            "perfil_alumno/{nombre}",
            arguments = listOf(navArgument("nombre") { type = NavType.StringType })
        ) { backStackEntry ->
            val nombre = backStackEntry.arguments?.getString("nombre") ?: "Alumno"
            PerfilAlumnoScreen(
                nombre = nombre,
                onLogout = {

                    // Volver al login limpiando el back stack
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}